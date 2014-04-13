package com.oohish.peermessages

import akka.util.ByteIterator
import akka.util.ByteString

object GetAddr extends MessagePayloadReader[GetAddr] {

  def decode(it: ByteIterator) = {
    GetAddr()
  }

}

case class GetAddr() extends MessagePayload {

  def encode = ByteString.empty

}