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
      if (buf.length >= length) { // TODO: check checksum
        codec.decode(buf.toBitVector).foreach {
          case (bits, msg) =>
            context.parent ! MessageDecoder.DecodedMessage(msg)
        }
        context.stop(self)
      }
    }
  }

}