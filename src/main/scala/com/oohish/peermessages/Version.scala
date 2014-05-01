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
      int32_t.decode(it).n,
      uint64_t.decode(it).n,
      int64_t.decode(it).n,
      NetworkAddress.decode(it),
      NetworkAddress.decode(it),
      uint64_t.decode(it).n,
      VarStr.decode(it).s,
      int32_t.decode(it).n)
  }

}

case class Version(
  version: Int,
  services: BigInt,
  timestamp: Long,
  addr_recv: NetworkAddress,
  addr_from: NetworkAddress,
  nonce: BigInt,
  user_agent: String,
  start_height: Int) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= int32_t(version).encode
    bb ++= uint64_t(services).encode
    bb ++= int64_t(timestamp).encode
    bb ++= addr_recv.encode
    bb ++= addr_from.encode
    bb ++= uint64_t(nonce).encode
    bb ++= VarStr(user_agent).encode
    bb ++= int32_t(start_height).encode
    bb.result
  }

}