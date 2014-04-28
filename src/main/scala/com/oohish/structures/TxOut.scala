package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * TxOut
 */
object TxOut extends StructureReader[TxOut] {

  def decode(it: ByteIterator): TxOut = {
    TxOut(
      int64_t.decode(it),
      new VarStructReader(uchar).decode(it).seq)
  }
}

case class TxOut(value: int64_t, pk_script: List[uchar]) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= value.encode
    bb ++= VarStruct(pk_script).encode
    bb.result
  }

}