package com.oohish.peermessages

import com.oohish.structures.uint64_t

import akka.util.ByteIterator
import akka.util.ByteString

object Ping extends MessagePayloadReader[Ping] {

  def decode(it: ByteIterator) = {
    Ping(
      uint64_t.decode(it).n)
  }

}

case class Ping(
  nonce: BigInt) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= uint64_t(nonce).encode
    bb.result
  }

}