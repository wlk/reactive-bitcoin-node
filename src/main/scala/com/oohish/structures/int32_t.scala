package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * int32_t
 */
object int32_t extends StructureReader[int32_t] {

  def decode(it: ByteIterator): int32_t = {
    val n = it.getInt
    int32_t(n)
  }
}

case class int32_t(n: Int) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putInt(n)
    bb.result
  }

}