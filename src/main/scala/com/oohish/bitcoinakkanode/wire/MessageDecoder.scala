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

object MessageDecoder {
  def props(magic: Long) =
    Props(classOf[MessageDecoder], magic)

  case class DecodedMessage(msg: Message)

}

class MessageDecoder(magic: Long) extends Actor with ActorLogging {
  import MessageDecoder._

  def receive = ready

  def ready: Actor.Receive = {
    case Tcp.Received(data) => {
      val bits = BitVector(data)

      val x = for {
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

      x.foreach {
        case (c, l, ch, p) =>
          val pd = context.actorOf(PayloadDecoder.props(c, l, ch))
          context.become(decoding(pd))
          context.watch(pd)
          pd ! PayloadDecoder.RawBytes(p.toByteVector)
      }
    }
  }

  def decoding(payloadDecoder: ActorRef): Actor.Receive = {
    case Tcp.Received(data) =>
      val bits = BitVector(data)
      payloadDecoder ! PayloadDecoder.RawBytes(bits.toByteVector)
    case DecodedMessage(msg) =>
      context.parent ! DecodedMessage(msg)
    case Terminated(ref) =>
      context.become(ready)
  }
}