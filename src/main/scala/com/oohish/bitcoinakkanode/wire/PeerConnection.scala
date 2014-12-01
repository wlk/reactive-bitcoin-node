package com.oohish.bitcoinakkanode.wire

import com.oohish.bitcoinakkanode.wire.MessageDecoder.DecodedMessage
import com.oohish.bitcoinakkanode.wire.MessageEncoder.EncodedMessage
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.io.Tcp
import akka.util.ByteString
import scodec.bits.ByteVector

object PeerConnection {
  def props(
    tcpConn: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[PeerConnection], tcpConn, networkParameters)

  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class PeerConnection(
  tcpConn: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import PeerConnection._

  val decoder = context.actorOf(MessageDecoder.props(networkParameters.packetMagic), "messageDecoder")
  val encoder = context.actorOf(MessageEncoder.props(networkParameters.packetMagic), "messageEncoder")

  def receive = {
    case OutgoingMessage(msg) =>
      log.debug("received outgoing message: " + msg)
      encoder ! msg
    case EncodedMessage(b) =>
      log.debug("received encoded message: " + ByteVector(b))
      tcpConn ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      log.debug("received decoded message: " + msg)
      context.parent ! msg
    case Tcp.Received(data) =>
      log.debug("received tcp bytes: " + ByteVector(data))
      decoder ! Tcp.Received(data)
    case Tcp.CommandFailed(w: Tcp.Write) =>
      log.debug("write failed")
    case Terminated(pc) =>
      log.debug("btc connection closed")
      tcpConn ! Tcp.Close
    case _: Tcp.ConnectionClosed =>
      log.debug("connection closed")
      context stop self
  }

}