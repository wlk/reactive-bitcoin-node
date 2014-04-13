package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * TxIn
 */
object TxIn extends StructureReader[TxIn] {

  def decode(it: ByteIterator): TxIn = {
    TxIn(
      OutPoint.decode(it),
      new VarStructReader(uchar).decode(it),
      uint32_t.decode(it))
  }
}

case class TxIn(
  previous_output: OutPoint,
  sig_script: VarStruct[uchar],
  sequence: uint32_t) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= previous_output.encode
    bb ++= sig_script.encode
    bb ++= sequence.encode
    bb.result
  }

}