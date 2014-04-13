package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * uint8_t
 */
object uint8_t extends StructureReader[uint8_t] {

  def decode(it: ByteIterator): uint8_t = {
    val n = it.getLongPart(1)
    uint8_t(n)
  }
}

case class uint8_t(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLongPart(n, 1)
    bb.result
  }

}