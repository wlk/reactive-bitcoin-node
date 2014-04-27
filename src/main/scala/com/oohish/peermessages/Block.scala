package com.oohish.peermessages

import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import akka.util.ByteString
import akka.util.ByteIterator
import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader
import java.security.MessageDigest

object Block extends MessagePayloadReader[Block] {

  def decode(it: ByteIterator) = {
    Block(
      uint32_t.decode(it),
      char32.decode(it),
      char32.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it),
      new VarStructReader(Tx).decode(it))
  }

}

case class Block(
  version: uint32_t,
  prev_block: char32,
  merkle_root: char32,
  timestamp: uint32_t,
  bits: uint32_t,
  nonce: uint32_t,
  txns: VarStruct[Tx]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= prev_block.encode
    bb ++= merkle_root.encode
    bb ++= timestamp.encode
    bb ++= bits.encode
    bb ++= nonce.encode
    bb ++= txns.encode
    bb.result
  }

  /**
   * Copy of the block without any transactions.
   */
  def toHeader(): Block = {
    copy(txns = new VarStruct[Tx](List()))
  }

  /**
   * Calculate the hash of a block.
   */
  def hash(): char32 = {
    val bh = toHeader()

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

}