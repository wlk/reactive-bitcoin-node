package com.oohish.peermessages

import com.oohish.structures.VarStr

import akka.util.ByteIterator
import akka.util.ByteString

object Alert extends MessagePayloadReader[Alert] {

  def decode(it: ByteIterator) = {
    Alert(
      VarStr.decode(it),
      VarStr.decode(it))
  }

}

case class Alert(
  payload: VarStr,
  signature: VarStr) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= payload.encode
    bb ++= signature.encode
    bb.result
  }

}