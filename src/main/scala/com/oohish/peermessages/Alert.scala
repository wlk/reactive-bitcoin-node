package com.oohish.peermessages

import com.oohish.structures.VarStr

import akka.util.ByteIterator
import akka.util.ByteString

object Alert extends MessagePayloadReader[Alert] {

  def decode(it: ByteIterator) = {
    Alert(
      VarStr.decode(it).s,
      VarStr.decode(it).s)
  }

}

case class Alert(
  payload: String,
  signature: String) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= VarStr(payload).encode
    bb ++= VarStr(signature).encode
    bb.result
  }

}