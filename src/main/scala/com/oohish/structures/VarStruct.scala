package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

class VarStructReader[T <: Structure](dtr: StructureReader[T]) extends StructureReader[VarStruct[T]] {

  def decode(it: ByteIterator): VarStruct[T] = {

    val n: Long = VarInt.decode(it).n

    val seq = for (i <- List.range(0, n)) yield dtr.decode(it)
    VarStruct(seq)
  }

}

case class VarStruct[T <: Structure](seq: List[T]) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    bb ++= VarInt(seq.length).encode

    for (item <- seq) {
      bb ++= item.encode
    }
    bb.result
  }

}