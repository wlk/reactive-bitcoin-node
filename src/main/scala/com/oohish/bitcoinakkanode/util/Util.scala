package com.oohish.bitcoinakkanode.util

import scala.util.Random
import java.net.InetSocketAddress
import com.oohish.bitcoinscodec.structures.IPV6
import com.oohish.bitcoinscodec.structures.IPV4
import scodec.bits.ByteVector
import com.oohish.bitcoinscodec.structures.NetworkAddress
import java.net.InetAddress
import com.oohish.bitcoinscodec.structures.Port

object Util {

  def genNonce(): BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

  def networkAddress(
    services: BigInt,
    socketAddr: InetSocketAddress): NetworkAddress = {
    NetworkAddress(
      services,
      ip(socketAddr.getAddress()),
      Port(socketAddr.getPort()))
  }

  def ip(addr: InetAddress): Either[IPV4, IPV6] = {
    val bytes = ByteVector(addr.getAddress())
    if (bytes.length == 4)
      Left(IPV4(bytes))
    else
      Right(IPV6(bytes))
  }

}