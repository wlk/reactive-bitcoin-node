package com.oohish.bitcoinakkanode.wire

import com.oohish.bitcoinakkanode.wire.MessageDecoder.DecodedMessage
import com.oohish.bitcoinakkanode.wire.MessageEncoder.EncodedMessage
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp
import akka.util.ByteString

object MessageTranslator {
  def props(
    tcpConn: ActorRef,
    msgHandler: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[MessageTranslator], tcpConn, networkParameters)

  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class MessageTranslator(
  tcpConn: ActorRef,
  msgHandler: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import MessageTranslator._

  val decoder = context.actorOf(MessageDecoder.props(networkParameters.packetMagic), "messageDecoder")
  val encoder = context.actorOf(MessageEncoder.props(networkParameters.packetMagic), "messageEncoder")

  def receive = {
    case msg: Message =>
      encoder ! msg
    case Tcp.Received(data) =>
      decoder ! Tcp.Received(data)
    case EncodedMessage(b) =>
      tcpConn ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      msgHandler ! msg
  }

}