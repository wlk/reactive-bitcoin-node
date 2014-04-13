package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * char
 */
object char extends StructureReader[char] {

  def decode(it: ByteIterator): char = {
    val byte = it.getByte
    char(byte)
  }
}

case class char(c: Byte) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putByte(c)
    bb.result
  }

}