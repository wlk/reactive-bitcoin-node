package com.oohish.chain

import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Verack
import com.oohish.structures.uint32_t
import com.oohish.wire.BTCConnection.Outgoing
import com.oohish.wire.NetworkParameters
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.pipe
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Block
import scala.util.Try
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object FullBlockChain {

  def props(networkParams: NetworkParameters) =
    Props(classOf[FullBlockChain], networkParams)

}

class FullBlockChain(networkParams: NetworkParameters) extends Actor with ActorLogging {

  import context.dispatcher

  val store: BlockStore = new MemoryBlockStore()
  val genesis = networkParams.genesisBlock.toHeader
  val sb = StoredBlock(genesis, 0)
  store.put(sb)
  store.setChainHead(sb)

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
      val futureAdded = addBlocks(h.seq)

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

  /*
   * Try to add a new block to the block store.
   */
  def addBlock(b: Block): Future[Try[Unit]] = {
    log.debug("adding block: " + b)
    for {
      maybePrevBlock <- store.get(b.prev_block)
      inserted <- {
        val tryPrev = Try { maybePrevBlock.get }
        tryPrev match {
          case Success(prevBlock) => {
            val sb = StoredBlock(b, prevBlock.height + 1)
            log.debug("stored block: " + sb)
            val ret = store.put(sb).map(u => Success(u))
            if (sb.height > store.getChainHead.get.height) {
              store.setChainHead(sb)
              log.info("chain height: " + sb.height + ", last existing block hash: " + Chain.blockHash(sb.block))
            }
            ret
          }
          case Failure(e) => {
            Future(Failure(e))
          }
        }
      }
    } yield inserted
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

    log.debug("calling addBlocksHelper")
    addBlocksHelper(Future(Success()), blocks)
  }

}