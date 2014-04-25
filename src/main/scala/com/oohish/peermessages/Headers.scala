package com.oohish.peermessages

import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader

import akka.util.ByteIterator
import akka.util.ByteString

object Headers extends MessagePayloadReader[Headers] {

  def decode(it: ByteIterator) = {
    Headers(
      new VarStructReader(Block).decode(it))
  }

}

case class Headers(
  headers: VarStruct[Block]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= headers.encode
    bb.result
  }

}