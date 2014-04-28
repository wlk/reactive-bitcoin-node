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
      uint32_t.decode(it),
      new VarStructReader(char32).decode(it).seq,
      char32.decode(it))
  }

}

case class GetBlocks(
  version: uint32_t,
  block_locator: List[char32],
  hash_stop: char32) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= VarStruct(block_locator).encode
    bb ++= hash_stop.encode
    bb.result
  }

}