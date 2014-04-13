package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * uint64_t
 */
object uint64_t extends StructureReader[uint64_t] {

  def decode(it: ByteIterator): uint64_t = {
    val unsignedLong = it.getLong
    val n = asBigInt(unsignedLong)
    uint64_t(n)
  }

  def asBigInt(unsignedLong: Long): BigInt =
    (BigInt(unsignedLong >>> 1) << 1) + (unsignedLong & 1)

  def asLong(n: BigInt): Long = {
    val smallestBit = (n & 1).toLong
    ((n >> 1).toLong << 1) | smallestBit
  }

}

case class uint64_t(n: BigInt) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    val longN = uint64_t.asLong(n)
    bb.putLong(longN)
    bb.result
  }

}