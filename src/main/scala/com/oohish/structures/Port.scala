package com.oohish.structures

import java.nio.ByteOrder

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * Port
 */
object Port extends StructureReader[Port] {

  implicit override val byteOrder = ByteOrder.BIG_ENDIAN

  def decode(it: ByteIterator): Port = {
    val n = it.getLongPart(2)
    Port(n)
  }
}

case class Port(n: Long) extends Structure {

  implicit override val byteOrder = ByteOrder.BIG_ENDIAN

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLongPart(n, 2)
    bb.result
  }

}