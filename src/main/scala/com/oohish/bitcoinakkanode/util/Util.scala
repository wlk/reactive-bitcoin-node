package com.oohish.bitcoinakkanode.util

import scala.util.Random
import scodec.bits.ByteVector
import java.net.InetAddress
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.messages.Block
import java.security.MessageDigest
import com.oohish.bitcoinscodec.structures.BlockHeader
import scodec.bits.BitVector

object Util {

  def genNonce(): BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

  def hash(bytes: Array[Byte]): Hash = {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hash1 = messageDigest.digest(bytes)
    val hash2 = messageDigest.digest(hash1)
    Hash(ByteVector(hash2).reverse)
  }

  def blockHash(b: Block): Hash = {
    val bytes = BlockHeader.codec.encode(b.block_header)
      .getOrElse(BitVector.empty).toByteArray
    Util.hash(bytes)
  }

}