package com.oohish.bitcoinakkanode.wire

import scodec.Codec
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorLogging
import com.oohish.bitcoinscodec.structures.Message.Message
import scodec.bits.BitVector
import akka.io.Tcp
import akka.util.ByteString
import scodec.bits.ByteVector
import com.oohish.bitcoinscodec.structures.Message

object PayloadDecoder {
  def props(
    codec: Codec[_ <: Message],
    length: Long,
    chksum: Long) =
    Props(classOf[PayloadDecoder], codec, length, chksum)

  case class RawBytes(data: ByteVector)
}

class PayloadDecoder(
  codec: Codec[_ <: Message],
  length: Long,
  chksum: Long) extends Actor with ActorLogging {

  var buf = ByteVector.empty

  def receive = {
    case PayloadDecoder.RawBytes(data) => {
      buf ++= data
      if (buf.length >= length) {
        val payloadBytes = buf.take(length.toInt)
        if (Message.checksum(payloadBytes) == chksum) {
          decodePayload(payloadBytes)
        } else {
          log.info("bad checksum")
        }
        context.stop(self)
      }
    }
  }

  def decodePayload(bytes: ByteVector): Unit = {
    codec.decode(bytes.toBitVector).foreach {
      case (bits, msg) =>
        context.parent ! MessageDecoder.DecodedMessage(msg)
    }
  }

}