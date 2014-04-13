package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator

/*
 * int64_t
 */
object int64_t extends StructureReader[int64_t] {

  def decode(it: ByteIterator): int64_t = {
    val n = it.getLong
    int64_t(n)
  }
}

case class int64_t(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb.putLong(n)
    bb.result
  }

}