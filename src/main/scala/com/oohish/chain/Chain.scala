package com.oohish.chain

import java.security.MessageDigest
import akka.util.ByteString
import com.oohish.structures.char32
import com.oohish.util.HexBytesUtil
import com.oohish.peermessages.Block
import com.oohish.structures.VarStruct
import com.oohish.peermessages.Tx
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Chain {

  /**
   * Calculate the hash of a block.
   */
  def blockHash(block: Block): char32 = {
    val bb = ByteString.newBuilder
    bb ++= block.version.encode
    bb ++= block.prev_block.encode
    bb ++= block.merkle_root.encode
    bb ++= block.timestamp.encode
    bb ++= block.bits.encode
    bb ++= block.nonce.encode
    val hashByteString = bb.result

    val messageDigest = MessageDigest.getInstance("SHA-256")
    val headerBytes: Array[Byte] = hashByteString.compact.toParArray.toArray
    val hash1 = messageDigest.digest(headerBytes)
    val hash2 = messageDigest.digest(hash1)
    char32(hash2.toList.reverse)
  }

  /**
   *   returns the list of indexes of blocks to be used in the block locator.
   */
  def blockLocatorIndices(chainLength: Int): List[Int] = {
    (0 :: blockLocatorIndicesHelper(List(), chainLength - 1, 1, 0)).reverse
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

  /////////////////////////////////
  // new attempt at block locator

  def serialiseFutures[A, B](l: Iterable[A])(fn: A ⇒ Future[B])(implicit ec: ExecutionContext): Future[List[B]] =
    l.foldLeft(Future(List.empty[B])) {
      (previousFuture, next) ⇒
        for {
          previousResults ← previousFuture
          next ← fn(next)
        } yield previousResults :+ next
    }

  def prevStoredBlock(store: BlockStore, sb: StoredBlock): Future[Option[StoredBlock]] = {
    store.get(sb.block.prev_block)
  }

  def blockLocator(store: BlockStore)(implicit ec: ExecutionContext): Future[VarStruct[char32]] = {
    val height = store.getChainHead.map(_.height).getOrElse(0)
    val locatorIndices = Chain.blockLocatorIndices(height)

    // TODO
    // serialiseFutures()

    def blockLocatorHelper(acc: Future[List[StoredBlock]]): Future[List[StoredBlock]] = {
      acc.flatMap { lst =>
        val h = lst.head
        prevStoredBlock(store, h).map { maybeBlock =>
          maybeBlock.map { block =>
            if (block.height == 0) {
              block :: lst
            } else if (locatorIndices.contains(block.height)) {
              block :: lst
            } else {
              lst
            }
          }.getOrElse(lst)
        }
      }
    }

    blockLocatorHelper(Future(List(store.getChainHead.get))).map { blockList =>
      val x = blockList.map { block =>
        Chain.blockHash(block.block)
      }
      VarStruct[char32](x)
    }

  }

  val emptyHashStop = char32(HexBytesUtil.hex2bytes("0000000000000000000000000000000000000000000000000000000000000000").toList)

}