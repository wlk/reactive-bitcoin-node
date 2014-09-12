package com.oohish.chain

import scala.concurrent.Future
import com.oohish.peermessages.Block
import scala.util.Try
import scala.util.Success
import scala.concurrent.ExecutionContext

class BlockChain(store: BlockStore)(implicit ec: ExecutionContext) {

  /*
   * Add a new block to the block store.
   */
  def addBlock(b: Block, isGenesis: Boolean = false): Future[Try[Unit]] = {
    for {
      maybeChainHead <- {
        store.getChainHead
      }
      maybePrevBlock <- {
        store.get(b.prev_block)
      }
      maybeStoredBlock <- {
        maybeStoreBlock(maybePrevBlock, b, isGenesis)
      }
      updatedChainHead <- {
        maybeUpdateChainHead(maybeStoredBlock, maybeChainHead)
      }
    } yield Try(maybeStoredBlock.get)
  }

  private def maybeStoreBlock(maybePrevBlock: Option[StoredBlock], block: Block, isGenesis: Boolean): Future[Option[StoredBlock]] = {
    val x = maybePrevBlock.map { prevBlock =>
      val sb = StoredBlock(block, prevBlock.height + 1)
      store.put(sb).map(_ => Some(sb))
    }.getOrElse {
      if (isGenesis) {
        val sb = StoredBlock(block, 0)
        store.put(sb).map(_ => Some(sb))
      } else {
        Future(None)
      }
    }
    x.recover { case _ => None }
  }

  private def maybeUpdateChainHead(maybeStoredBlock: Option[StoredBlock], maybeChainHead: Option[StoredBlock]): Future[Boolean] = {
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

    addBlocksHelper(Future(Success(())), blocks)
  }

  /*
   * get the chain head.
   */
  def getChainHead(): Future[Option[StoredBlock]] = {
    store.getChainHead
  }

}