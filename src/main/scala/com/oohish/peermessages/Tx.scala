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
      uint32_t.decode(it),
      new VarStructReader(TxIn).decode(it),
      new VarStructReader(TxOut).decode(it),
      uint32_t.decode(it))
  }

}

case class Tx(
  version: uint32_t,
  tx_in: VarStruct[TxIn],
  tx_out: VarStruct[TxOut],
  lock_time: uint32_t) extends MessagePayload {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= tx_in.encode
    bb ++= tx_out.encode
    bb ++= lock_time.encode
    bb.result
  }

}