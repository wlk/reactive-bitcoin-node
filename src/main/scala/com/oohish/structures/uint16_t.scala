package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * uint16_t
 */
object uint16_t extends StructureReader[uint16_t] {

  def decode(it: ByteIterator): uint16_t = {
    val n = it.getLongPart(2)
    uint16_t(n)
  }
}

case class uint16_t(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLongPart(n, 2)
    bb.result
  }

}