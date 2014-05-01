package com.oohish.peermessages

import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader
import com.oohish.structures.char32
import com.oohish.structures.uint32_t

import akka.util.ByteIterator
import akka.util.ByteString

object GetBlocks extends MessagePayloadReader[GetBlocks] {

  def decode(it: ByteIterator) = {
    GetBlocks(
      uint32_t.decode(it).n,
      new VarStructReader(char32).decode(it).seq.map(_.s),
      char32.decode(it).s)
  }

}

case class GetBlocks(
  version: Long,
  block_locator: List[String],
  hash_stop: String) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= uint32_t(version).encode
    bb ++= VarStruct(block_locator.map(char32(_))).encode
    bb ++= char32(hash_stop).encode
    bb.result
  }

}