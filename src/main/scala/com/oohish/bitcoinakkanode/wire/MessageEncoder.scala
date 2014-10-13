package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.io.Tcp
import scodec.bits.BitVector
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.Message._
import akka.util.ByteString

object MessageEncoder {
  def props(magic: Long) =
    Props(classOf[MessageEncoder], magic)

  case class EncodedMessage(bytes: ByteString)
}

class MessageEncoder(magic: Long) extends Actor with ActorLogging {
  import MessageEncoder._

  val version = 1

  def receive = {
    case msg: Message => {
      Message.codec(magic, version).encode(msg)
        .foreach { b =>
          val bytes = ByteString(b.toByteBuffer)
          context.parent ! EncodedMessage(bytes)
        }
    }
  }
}