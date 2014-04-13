package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

class SequenceReader[T <: Structure](n: Long, dtr: StructureReader[T]) extends StructureReader[Sequence[T]] {

  def decode(it: ByteIterator): Sequence[T] = {
    val seq = for (i <- List.range(0, n)) yield dtr.decode(it)
    Sequence(seq)
  }

}

case class Sequence[T <: Structure](seq: List[T]) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    for (item <- seq) {
      bb ++= item.encode
    }
    bb.result
  }

}