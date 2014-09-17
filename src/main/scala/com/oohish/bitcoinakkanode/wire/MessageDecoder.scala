package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.io.Tcp
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.Message._
import scodec.bits.BitVector

object MessageDecoder {
  def props(magic: Long) =
    Props(classOf[MessageDecoder], magic)

  case class DecodedMessage(msg: Message)

}

class MessageDecoder(magic: Long) extends Actor with ActorLogging {
  import MessageDecoder._

  var buf = BitVector.empty

  def receive = {
    case Tcp.Received(data) => {
      val bytes = BitVector(data)
      Message.codec(0L).decode(bytes)
        .foreach {
          case (b, m) =>
            buf = b
            context.parent ! DecodedMessage(m)
        }
      buf = BitVector.empty
    }
  }
}