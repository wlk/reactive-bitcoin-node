package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * uint32_t
 */
object uint32_t extends StructureReader[uint32_t] {

  def decode(it: ByteIterator): uint32_t = {
    val n = it.getLongPart(4)
    val m = n & 0x00000000ffffffffL
    uint32_t(m)
  }
}

case class uint32_t(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLongPart(n, 4)
    bb.result
  }
}