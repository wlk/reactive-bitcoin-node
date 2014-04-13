package com.oohish.peermessages

import akka.util.ByteIterator
import akka.util.ByteString

object MemPool extends MessagePayloadReader[MemPool] {

  def decode(it: ByteIterator) = {
    MemPool()
  }

}

case class MemPool() extends MessagePayload {

  def encode = ByteString.empty

}