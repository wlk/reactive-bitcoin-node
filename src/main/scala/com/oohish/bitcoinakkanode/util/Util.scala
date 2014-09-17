package com.oohish.bitcoinakkanode.util

import scala.util.Random
import java.net.InetSocketAddress
import com.oohish.bitcoinscodec.structures.IPV6
import com.oohish.bitcoinscodec.structures.IPV4
import scodec.bits.ByteVector

object Util {

  def genNonce(): BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

  def networkAddress(peer: InetSocketAddress): Either[IPV4, IPV6] = {
    val bytes = ByteVector(peer.getAddress().getAddress())
    if (bytes.length == 4)
      Left(IPV4(bytes))
    else
      Right(IPV6(bytes))
  }

}