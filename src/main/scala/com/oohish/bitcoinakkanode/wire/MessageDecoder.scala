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

  def receive = ready(ByteVector.empty)

  def ready(buf: ByteVector): Actor.Receive = {
    case Tcp.Received(data) => {
      decodeCommand(ByteVector(data)).foreach {
        case (c, l, ch, p) =>
          log.info("becoming decoding with length {}", l)
          context.become(decoding(c, l, ch, ByteVector.empty))
          self ! Tcp.Received(ByteString(p.toByteBuffer))
      }
    }
  }

  def decoding(
    codec: Codec[_ <: Message],
    length: Long,
    chksum: Long,
    buf: ByteVector): Actor.Receive = {
    case Tcp.Received(data) =>
      val newBuff = buf ++ ByteVector(data)
      log.info("buf length: {}", newBuff.length)
      if (newBuff.length >= length) {
        val (payloadBytes, rest) = newBuff.splitAt(length.toInt)
        val x = decodePayload(codec, length, chksum, newBuff)
        log.info("decodePayload result: {}", x)
        x.foreach {
          case (rest, msg) =>
            context.parent ! MessageDecoder.DecodedMessage(msg)
        }
        log.info("becoming ready")
        context.become(ready(rest))
      } else {
        context.become(decoding(codec, length, chksum, newBuff))
      }
  }

  def decodeCommand(bits: ByteVector) = {
    for {
      m <- uint32L.decode(bits.toBitVector) match {
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
    buf: ByteVector): scalaz.\/[String, (BitVector, Message)] = {
    if (Message.checksum(buf) == chksum) {
      log.info("chksum good, decoding payload with length {}", length)
      log.info("is codec a Headers codec?: {}", Headers.codec == codec)
      codec.decode(buf.toBitVector)
    } else {
      -\/(("checksum did not match."))
    }
  }

}