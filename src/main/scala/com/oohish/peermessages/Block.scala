package com.oohish.peermessages

import java.security.MessageDigest

import scala.collection.parallel.traversable2ops

import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader
import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import com.oohish.util.HexBytesUtil

import akka.util.ByteIterator
import akka.util.ByteString

object Block extends MessagePayloadReader[Block] {

  def decode(it: ByteIterator) = {
    Block(
      uint32_t.decode(it).n,
      char32.decode(it).s,
      char32.decode(it).s,
      uint32_t.decode(it).n,
      uint32_t.decode(it).n,
      uint32_t.decode(it).n,
      new VarStructReader(Tx).decode(it).seq)
  }

}

case class Block(
  version: Long,
  prev_block: String,
  merkle_root: String,
  timestamp: Long,
  bits: Long,
  nonce: Long,
  txns: List[Tx]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= uint32_t(version).encode
    bb ++= char32(prev_block).encode
    bb ++= char32(merkle_root).encode
    bb ++= uint32_t(timestamp).encode
    bb ++= uint32_t(bits).encode
    bb ++= uint32_t(nonce).encode
    bb ++= VarStruct(txns).encode
    bb.result
  }

  /**
   * Copy of the block without any transactions.
   */
  def toHeader(): Block = {
    copy(txns = List())
  }

  /**
   * Calculate the hash of a block.
   */
  def hash(): String = {
    val bh = toHeader()

    val bb = ByteString.newBuilder
    bb ++= uint32_t(bh.version).encode
    bb ++= char32(bh.prev_block).encode
    bb ++= char32(bh.merkle_root).encode
    bb ++= uint32_t(bh.timestamp).encode
    bb ++= uint32_t(bh.bits).encode
    bb ++= uint32_t(bh.nonce).encode
    val hashByteString = bb.result

    val messageDigest = MessageDigest.getInstance("SHA-256")
    val headerBytes: Array[Byte] = hashByteString.compact.toParArray.toArray
    val hash1 = messageDigest.digest(headerBytes)
    val hash2 = messageDigest.digest(hash1)
    HexBytesUtil.bytes2hex(hash2.reverse)
  }

}