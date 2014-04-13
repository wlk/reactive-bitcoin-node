package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

object NetworkAddress extends StructureReader[NetworkAddress] {

  def decode(it: ByteIterator): NetworkAddress = {
    val time = uint32_t.decode(it)
    val services = uint64_t.decode(it)
    val ip = IP.decode(it)
    val port = Port.decode(it)
    NetworkAddress(time, services, ip, port)
  }

}

object NetworkAddressInVersion extends StructureReader[NetworkAddressInVersion] {

  def decode(it: ByteIterator): NetworkAddressInVersion = {
    val services = uint64_t.decode(it)
    val ip = IP.decode(it)
    val port = Port.decode(it)
    NetworkAddressInVersion(services, ip, port)
  }

}

case class NetworkAddress(
  time: uint32_t,
  services: uint64_t,
  ip: IP,
  port: Port) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= time.encode
    bb ++= services.encode
    bb ++= ip.encode
    bb ++= port.encode
    bb.result
  }

}

case class NetworkAddressInVersion(
  services: uint64_t,
  ip: IP,
  port: Port) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= services.encode
    bb ++= ip.encode
    bb ++= port.encode
    bb.result
  }

}

