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
import scodec.bits.ByteVector

object PeerConnection {
  def props(
    tcpConn: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[PeerConnection], tcpConn, networkParameters)

  case class Register(ref: ActorRef)
  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class PeerConnection(
  tcpConn: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import PeerConnection._

  val decoder = context.actorOf(MessageDecoder.props(networkParameters.packetMagic), "messageDecoder")
  val encoder = context.actorOf(MessageEncoder.props(networkParameters.packetMagic), "messageEncoder")

  var listeners: Set[ActorRef] = Set.empty

  def receive = {
    case Register(ref) =>
      listeners += ref
      context.watch(ref)
    case akka.actor.Terminated(ref) =>
      listeners -= ref
    case OutgoingMessage(msg) =>
      log.debug("received outgoing message: " + msg)
      encoder ! msg
    case EncodedMessage(b) =>
      log.debug("received encoded message: " + ByteVector(b))
      tcpConn ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      log.debug("received decoded message: " + msg)
      for (listener <- listeners) listener ! msg
    case Tcp.Received(data) =>
      log.debug("received tcp bytes: " + ByteVector(data))
      decoder ! Tcp.Received(data)
    case Tcp.CommandFailed(w: Tcp.Write) =>
      log.debug("write failed")
    case _: Tcp.ConnectionClosed =>
      log.debug("connection closed")
      context stop self
  }

}