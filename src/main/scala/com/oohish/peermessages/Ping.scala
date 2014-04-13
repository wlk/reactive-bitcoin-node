package com.oohish.peermessages

import com.oohish.structures.uint64_t

import akka.util.ByteIterator
import akka.util.ByteString

object Ping extends MessagePayloadReader[Ping] {

  def decode(it: ByteIterator) = {
    Ping(
      uint64_t.decode(it))
  }

}

case class Ping(
  nonce: uint64_t) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= nonce.encode
    bb.result
  }

}