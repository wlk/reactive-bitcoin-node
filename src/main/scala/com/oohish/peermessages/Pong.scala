package com.oohish.peermessages

import com.oohish.structures.uint64_t

import akka.util.ByteIterator
import akka.util.ByteString

object Pong extends MessagePayloadReader[Pong] {

  def decode(it: ByteIterator) = {
    Pong(
      uint64_t.decode(it))
  }

}

case class Pong(
  nonce: uint64_t) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= nonce.encode
    bb.result
  }

}