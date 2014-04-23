package com.oohish.chain

import java.security.MessageDigest
import com.oohish.structures.BlockHeader
import akka.util.ByteString
import com.oohish.structures.char32
import com.oohish.util.HexBytesUtil

object Chain {

  /**
   * Calculate the hash of a block header.
   */
  def blockHash(bh: BlockHeader): char32 = {
    val bb = ByteString.newBuilder
    bb ++= bh.version.encode
    bb ++= bh.prev_block.encode
    bb ++= bh.merkle_root.encode
    bb ++= bh.timestamp.encode
    bb ++= bh.bits.encode
    bb ++= bh.nonce.encode
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
    blockLocatorIndicesHelper(List(), chainLength - 1, 1, 0).reverse
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

  val emptyHashStop = char32(HexBytesUtil.hex2bytes("0000000000000000000000000000000000000000000000000000000000000000").toList)

}