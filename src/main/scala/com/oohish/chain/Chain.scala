package com.oohish.chain

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import com.oohish.structures.char32
import scala.util.Try
import scala.util.Success
import com.oohish.peermessages.Block

object Chain {

  /**
   *   returns the list of indexes of blocks to be used in the block locator.
   */
  def blockLocatorIndices(chainLength: Int): List[Int] = {
    0 :: blockLocatorIndicesHelper(List(), chainLength - 1, 1, 0)
  }

  /**
   * helper function for getting block locator indices.
   */
  private def blockLocatorIndicesHelper(acc: List[Int], i: Int, step: Int, start: Int): List[Int] = {
    if (i < 0) {
      acc
    } else if (start >= 10) {
      blockLocatorIndicesHelper(i :: acc, i - step, step * 2, start + 1)
    } else {
      blockLocatorIndicesHelper(i :: acc, i - step, step, start + 1)
    }
  }

  /*
   * return the previous storedblock from a given storedblock
   */
  def prevStoredBlock(store: BlockStore, sb: StoredBlock): Future[Option[StoredBlock]] = {
    store.get(sb.block.prev_block)
  }

  /*
   * useful function
   */
  def futureCons[T](flst: Future[List[T]], x: T)(implicit ec: ExecutionContext): Future[List[T]] = {
    flst.flatMap { lst =>
      Future(x :: lst)
    }
  }

  /*
   * return a block locator for the block store.
   */
  def blockLocator(store: BlockStore)(implicit ec: ExecutionContext): Future[List[char32]] = {
    store.getChainHead.flatMap { maybeSb =>
      val maxHeight = maybeSb.map(_.height).getOrElse(0)
      val locatorIndices = Chain.blockLocatorIndices(maxHeight + 1)

      def blockLocatorHelper(acc: Future[List[char32]], maybeCur: Option[StoredBlock]): Future[List[char32]] = {
        maybeCur.map { cur =>
          val futurePrev = prevStoredBlock(store, cur)
          futurePrev.flatMap { maybePrev =>
            if (cur.height == 0) {
              blockLocatorHelper(futureCons(acc, cur.block.hash), maybePrev)
            } else if (locatorIndices.contains(cur.height)) {
              blockLocatorHelper(futureCons(acc, cur.block.hash), maybePrev)
            } else {
              blockLocatorHelper(acc, maybePrev)
            }
          }
        }.getOrElse(acc)
      }

      blockLocatorHelper(Future(List()), maybeSb).map { blockList =>
        blockList.reverse
      }
    }

  }

  val emptyHashStop = char32("0000000000000000000000000000000000000000000000000000000000000000")

}