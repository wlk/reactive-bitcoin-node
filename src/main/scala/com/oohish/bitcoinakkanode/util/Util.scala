package com.oohish.bitcoinakkanode.util

import scala.util.Random
import scodec.bits.ByteVector
import java.net.InetAddress

object Util {

  def genNonce(): BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

}