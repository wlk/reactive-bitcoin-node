package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * OutPoint
 */
object OutPoint extends StructureReader[OutPoint] {

  def decode(it: ByteIterator): OutPoint = {
    OutPoint(
      char32.decode(it).s,
      uint32_t.decode(it).n)
  }
}

case class OutPoint(hash: String, index: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= char32(hash).encode
    bb ++= uint32_t(index).encode
    bb.result
  }

}