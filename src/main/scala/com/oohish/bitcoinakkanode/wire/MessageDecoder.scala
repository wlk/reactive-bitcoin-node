package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.io.Tcp
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.Message._
import scodec.bits.BitVector
import scodec.bits._
import scalaz.-\/
import scalaz.\/-
import scala.language.existentials
import scodec.codecs._
import akka.actor.ActorRef
import scodec.Codec
import akka.actor.Terminated
import akka.util.ByteString
import com.oohish.bitcoinscodec.messages.Headers

object MessageDecoder {
  def props(magic: Long) =
    Props(classOf[MessageDecoder], magic)

  case class DecodedMessage(msg: Message)

}

class MessageDecoder(magic: Long) extends Actor with ActorLogging {
  import MessageDecoder._

  val version = 1

  def receive = ready(BitVector.empty)

  def ready(buf: BitVector): Actor.Receive = {
    case Tcp.Received(data) => {
      Message.decodeHeader(BitVector(data), magic, version).foreach {
        case (c, l, ch, p, r) =>
          log.debug("becoming decoding with length {}", l)
          context.become(decoding(c, l, ch, BitVector.empty))
          self ! Tcp.Received(ByteString(p.toByteBuffer))
      }
    }
  }

  def decoding(
    command: ByteVector,
    length: Long,
    chksum: Long,
    buf: BitVector): Actor.Receive = {
    case Tcp.Received(data) =>
      val newBuff = buf ++ BitVector(data)
      log.debug("buf length: {}", newBuff.length / 8)
      if (newBuff.length / 8 >= length) {
        val (payloadBytes, rest) = newBuff.splitAt(length.toInt * 8)
        Message.decodePayload(payloadBytes, version, chksum, command).foreach {
          msg => context.parent ! MessageDecoder.DecodedMessage(msg)
        }
        log.debug("becoming ready")
        context.become(ready(rest))
      } else {
        context.become(decoding(command, length, chksum, newBuff))
      }
  }

}