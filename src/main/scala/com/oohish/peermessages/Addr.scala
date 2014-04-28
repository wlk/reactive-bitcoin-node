package com.oohish.peermessages

import com.oohish.structures.NetworkAddress
import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader

import akka.util.ByteIterator
import akka.util.ByteString

object Addr extends MessagePayloadReader[Addr] {

  def decode(it: ByteIterator) = {
    Addr(
      new VarStructReader(TimeNetworkAddress).decode(it).seq)
  }

}

case class Addr(
  addrs: List[TimeNetworkAddress]) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= VarStruct(addrs).encode
    bb.result
  }

}