package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * OutPoint
 */
object OutPoint extends StructureReader[OutPoint] {

  def decode(it: ByteIterator): OutPoint = {
    OutPoint(
      char32.decode(it),
      uint32_t.decode(it))
  }
}

case class OutPoint(hash: char32, index: uint32_t) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= hash.encode
    bb ++= index.encode
    bb.result
  }

}