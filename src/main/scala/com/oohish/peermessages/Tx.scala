package com.oohish.peermessages

import com.oohish.structures.TxIn
import com.oohish.structures.TxOut
import com.oohish.structures.VarStruct
import com.oohish.structures.VarStructReader
import com.oohish.structures.uint32_t

import akka.util.ByteIterator
import akka.util.ByteString

object Tx extends MessagePayloadReader[Tx] {

  def decode(it: ByteIterator) = {
    Tx(
      uint32_t.decode(it).n,
      new VarStructReader(TxIn).decode(it).seq,
      new VarStructReader(TxOut).decode(it).seq,
      uint32_t.decode(it).n)
  }

}

case class Tx(
  version: Long,
  tx_in: List[TxIn],
  tx_out: List[TxOut],
  lock_time: Long) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= uint32_t(version).encode
    bb ++= VarStruct(tx_in).encode
    bb ++= VarStruct(tx_out).encode
    bb ++= uint32_t(lock_time).encode
    bb.result
  }

}