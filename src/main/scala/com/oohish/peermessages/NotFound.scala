package com.oohish.peermessages

import com.oohish.structures.InvVect
import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader

import akka.util.ByteIterator
import akka.util.ByteString

object NotFound extends MessagePayloadReader[NotFound] {

  def decode(it: ByteIterator) = {
    NotFound(
      new VarStructReader(InvVect).decode(it).seq)
  }

}

case class NotFound(
  vectors: List[InvVect]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= VarStruct(vectors).encode
    bb.result
  }

}