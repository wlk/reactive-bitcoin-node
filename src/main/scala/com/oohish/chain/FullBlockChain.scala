package com.oohish.chain

import scala.concurrent.Future
import scala.util.Success
import scala.util.Try

import com.oohish.peermessages.Block
import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Verack
import com.oohish.structures.uint32_t
import com.oohish.wire.BTCConnection.Outgoing
import com.oohish.wire.NetworkParameters

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.pipe
import reactivemongo.api.MongoConnection

object FullBlockChain {

  def props(
    networkParams: NetworkParameters,
    conn: Option[MongoConnection]) =
    Props(classOf[FullBlockChain], networkParams, conn)

}

class FullBlockChain(
  networkParams: NetworkParameters,
  conn: Option[MongoConnection]) extends Actor with ActorLogging {

  import context.dispatcher

  // initialize the block store.
  val store: BlockStore = new MongoBlockStore(conn)
  addBlock(networkParams.genesisBlock.toHeader)

  def receive = {

    case Verack() => {
      log.info("FullBlockChain received Verack")
      val futureBL = Chain.blockLocator(store)
      val blockLocator = futureBL.map { bl =>
        Outgoing(
          GetHeaders(uint32_t(60002), bl, Chain.emptyHashStop))
      }
      blockLocator.pipeTo(sender)
    }

    case Headers(h) => {
      val newHeaders = h.seq
      log.info("FullBlockChain received Headers with seq length: " + newHeaders.length)

      log.debug("calling addBlocks")
      val futureAdded = addBlocks(h)

      // if new headers received, ask for more.
      if (h.seq.length > 0) {
        val futureBL = for {
          added <- futureAdded
          bl <- Chain.blockLocator(store)
        } yield Outgoing(
          GetHeaders(uint32_t(60002), bl, Chain.emptyHashStop))
        futureBL.pipeTo(sender)
      }
    }

  }

  def addGenesis(block: Block) = {

  }

  /*
   * Add a new block to the block store.
   */
  def addBlock(b: Block): Future[Try[Unit]] = {
    log.info("adding block")
    for {
      maybeChainHead <- {
        log.info("maybeChainHead")
        store.getChainHead
      }
      maybePrevBlock <- {
        log.info("maybePrevBlock")
        store.get(b.prev_block)
      }
      maybeStoredBlock <- {
        log.info("maybeStoredBlock")
        maybeStoreBlock(maybePrevBlock, b)
      }
      updatedChainHead <- {
        log.info("updatedChainHead")
        maybeUpdateChainHead(maybeStoredBlock, maybeChainHead)
      }
    } yield Try(maybeStoredBlock.get)
  }

  def maybeStoreBlock(maybePrevBlock: Option[StoredBlock], block: Block): Future[Option[StoredBlock]] = {
    val x = maybePrevBlock.map { prevBlock =>
      val sb = StoredBlock(block, prevBlock.height + 1)
      log.debug("stored block: " + sb)
      store.put(sb).map(_ => Some(sb))
    }.getOrElse {
      val sb = StoredBlock(block, 0)
      log.debug("stored block: " + sb)
      store.put(sb).map(_ => Some(sb))
    }
    x.recover { case _ => None }
  }

  def maybeUpdateChainHead(maybeStoredBlock: Option[StoredBlock], maybeChainHead: Option[StoredBlock]): Future[Boolean] = {
    maybeStoredBlock.map { storedBlock =>
      maybeChainHead.map { chainHead =>
        if (storedBlock.height > chainHead.height) {
          store.setChainHead(storedBlock).map(_ => true)
        } else {
          Future(false)
        }
      }.getOrElse(store.setChainHead(storedBlock).map(_ => true))
    }.getOrElse(Future(false))
  }

  /*
   * Add a list of blocks
   */
  def addBlocks(blocks: List[Block]) = {
    def addBlocksHelper(acc: Future[Try[Unit]], blks: List[Block]): Future[Try[Unit]] = {
      blks match {
        case Nil => acc
        case h :: t => {
          addBlock(h).flatMap { res =>
            addBlocksHelper(Future(res), t)
          }
        }
      }
    }

    log.info("calling addBlocksHelper")
    addBlocksHelper(Future(Success()), blocks)
  }

}