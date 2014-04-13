package com.oohish.peermessages

import com.oohish.structures.InvVect
import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader

import akka.util.ByteIterator
import akka.util.ByteString

object GetData extends MessagePayloadReader[GetData] {

  def decode(it: ByteIterator) = {
    GetData(new VarStructReader(InvVect).decode(it))
  }

}

case class GetData(
  vectors: VarStruct[InvVect]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= vectors.encode
    bb.result
  }

}