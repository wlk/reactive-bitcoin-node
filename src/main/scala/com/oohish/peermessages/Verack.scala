package com.oohish.peermessages

import akka.util.ByteIterator
import akka.util.ByteString

object Verack extends MessagePayloadReader[Verack] {

  def decode(it: ByteIterator) = {
    Verack()
  }

}

case class Verack() extends MessagePayload {

  def encode = ByteString.empty

}