package com.oohish.peermessages

import com.oohish.structures.NetworkAddress
import com.oohish.structures.VarStr
import com.oohish.structures.int32_t
import com.oohish.structures.int64_t
import com.oohish.structures.uint64_t

import akka.util.ByteIterator
import akka.util.ByteString

object Version extends MessagePayloadReader[Version] {

  def decode(it: ByteIterator) = {
    Version(
      int32_t.decode(it),
      uint64_t.decode(it),
      int64_t.decode(it),
      NetworkAddress.decode(it),
      NetworkAddress.decode(it),
      uint64_t.decode(it),
      VarStr.decode(it),
      int32_t.decode(it))
  }

}

case class Version(
  version: int32_t,
  services: uint64_t,
  timestamp: int64_t,
  addr_recv: NetworkAddress,
  addr_from: NetworkAddress,
  nonce: uint64_t,
  user_agent: VarStr,
  start_height: int32_t) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= services.encode
    bb ++= timestamp.encode
    bb ++= addr_recv.encode
    bb ++= addr_from.encode
    bb ++= nonce.encode
    bb ++= user_agent.encode
    bb ++= start_height.encode
    bb.result
  }

}