package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

object NetworkAddress extends StructureReader[NetworkAddress] {

  def decode(it: ByteIterator): NetworkAddress = {
    val services = uint64_t.decode(it).n
    val ip = IP.decode(it)
    val port = Port.decode(it)
    NetworkAddress(services, ip, port)
  }

}

case class NetworkAddress(
  services: BigInt,
  ip: IP,
  port: Port) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= uint64_t(services).encode
    bb ++= ip.encode
    bb ++= port.encode
    bb.result
  }

}

