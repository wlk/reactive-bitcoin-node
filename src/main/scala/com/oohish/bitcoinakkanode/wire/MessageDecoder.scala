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

  def receive = ready(BitVector.empty)

  def ready(buf: BitVector): Actor.Receive = {
    case Tcp.Received(data) => {
      decodeCommand(BitVector(data)).foreach {
        case (c, l, ch, p) =>
          log.debug("becoming decoding with length {}", l)
          context.become(decoding(c, l, ch, BitVector.empty))
          self ! Tcp.Received(ByteString(p.toByteBuffer))
      }
    }
  }

  def decoding(
    codec: Codec[_ <: Message],
    length: Long,
    chksum: Long,
    buf: BitVector): Actor.Receive = {
    case Tcp.Received(data) =>
      val newBuff = buf ++ BitVector(data)
      log.debug("buf length: {}", newBuff.length / 8)
      if (newBuff.length / 8 >= length) {
        val (payloadBytes, rest) = newBuff.splitAt(length.toInt * 8)
        val x = decodePayload(codec, length, chksum, newBuff)
        log.debug("decodePayload result: {}", x)
        x.foreach {
          case (rest, msg) =>
            context.parent ! MessageDecoder.DecodedMessage(msg)
        }
        log.debug("becoming ready")
        context.become(ready(rest))
      } else {
        context.become(decoding(codec, length, chksum, newBuff))
      }
  }

  def decodeCommand(bits: BitVector) = {
    for {
      m <- uint32L.decode(bits) match {
        case \/-((rem, mg)) =>
          if (mg == magic)
            \/-((rem, mg))
          else
            -\/(("magic did not match."))
        case -\/(err) => -\/(err)
      }
      (mrem, _) = m
      c <- payloadCodec.decode(mrem)
      (crem, command) = c
      l <- uint32L.decode(crem)
      (lrem, length) = l
      ch <- uint32L.decode(lrem)
      (chrem, chksum) = ch
      (payload, rest) = chrem.splitAt(length * 8)
    } yield (command, length, chksum, payload)
  }

  def decodePayload(
    codec: Codec[_ <: Message],
    length: Long,
    chksum: Long,
    buf: BitVector): scalaz.\/[String, (BitVector, Message)] = {

    log.debug("chksum found: {}, checksume expected: {}", Message.checksum(buf.toByteVector), chksum)
    if ((Message.checksum(buf.toByteVector) == chksum) || true) {
      log.debug("chksum good, decoding payload with length {}", length)
      codec.decode(buf)
    } else {
      -\/(("checksum did not match."))
    }
  }

}