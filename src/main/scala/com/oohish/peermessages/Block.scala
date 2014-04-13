package com.oohish.peermessages

import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import akka.util.ByteString
import akka.util.ByteIterator

object Block extends MessagePayloadReader[Block] {

  def decode(it: ByteIterator) = {
    Block(
      uint32_t.decode(it),
      char32.decode(it),
      char32.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it))
  }

}

case class Block(
  version: uint32_t,
  prev_block: char32,
  merkle_root: char32,
  timestamp: uint32_t,
  bits: uint32_t,
  nonce: uint32_t) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= prev_block.encode
    bb ++= merkle_root.encode
    bb ++= timestamp.encode
    bb ++= bits.encode
    bb ++= nonce.encode
    bb.result
  }

}