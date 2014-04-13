package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * uchar
 */
object uchar extends StructureReader[uchar] {

  def decode(it: ByteIterator): uchar = {
    val n = it.getLongPart(1)
    uchar(n)
  }
}

case class uchar(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLongPart(n, 1)
    bb.result
  }

}