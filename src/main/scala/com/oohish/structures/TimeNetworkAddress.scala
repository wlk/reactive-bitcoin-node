package com.oohish.peermessages

import com.oohish.structures.NetworkAddress
import com.oohish.structures.Structure
import com.oohish.structures.StructureReader
import com.oohish.structures.uint32_t

import akka.util.ByteIterator
import akka.util.ByteString

object TimeNetworkAddress extends StructureReader[TimeNetworkAddress] {

  def decode(it: ByteIterator) = {
    TimeNetworkAddress(
      uint32_t.decode(it),
      NetworkAddress.decode(it))
  }

}

case class TimeNetworkAddress(
  time: uint32_t,
  addr: NetworkAddress) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= time.encode
    bb ++= addr.encode
    bb.result
  }

}