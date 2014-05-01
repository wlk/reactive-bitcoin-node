package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * TxOut
 */
object TxOut extends StructureReader[TxOut] {

  def decode(it: ByteIterator): TxOut = {
    TxOut(
      int64_t.decode(it).n,
      Script.decode(it).s)
  }
}

case class TxOut(value: Long, pk_script: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= int64_t(value).encode
    bb ++= Script(pk_script).encode
    bb.result
  }

}