package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import com.oohish.bitcoinakkanode.wire.MessageDecoder.DecodedMessage
import com.oohish.bitcoinakkanode.wire.MessageEncoder.EncodedMessage
import com.oohish.bitcoinscodec.structures.Message.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.io.Tcp
import akka.util.ByteString
import scodec.bits.ByteVector

object TCPConnection {
  def props(
    manager: ActorRef,
    connection: ActorRef,
    remote: InetSocketAddress,
    local: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[TCPConnection], manager, connection, remote, local, networkParams)

  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class TCPConnection(
  manager: ActorRef,
  connection: ActorRef,
  remote: InetSocketAddress,
  local: InetSocketAddress,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import TCPConnection._
  import akka.actor.Terminated

  val pc = context.actorOf(PeerConnection.props(
    manager, remote, local, networkParams))
  context.watch(pc)

  val decoder = context.actorOf(MessageDecoder.props(networkParams.packetMagic), name = "messageDecoder")
  val encoder = context.actorOf(MessageEncoder.props(networkParams.packetMagic), name = "messageEncoder")

  def receive = {
    case OutgoingMessage(msg) =>
      log.debug("received outgoing message: " + msg)
      encoder ! msg
    case EncodedMessage(b) =>
      log.debug("received encoded message: " + ByteVector(b))
      connection ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      log.debug("received decoded message: " + msg)
      pc ! msg
    case Tcp.Received(data) =>
      log.debug("received tcp bytes: " + ByteVector(data))
      decoder ! Tcp.Received(data)
    case Tcp.CommandFailed(w: Tcp.Write) =>
      log.debug("write failed")
    case Terminated(pc) =>
      log.debug("btc connection closed")
      connection ! Tcp.Close
    case "close" =>
      log.debug("closing connection")
      connection ! Tcp.Close
    case _: Tcp.ConnectionClosed =>
      log.debug("connection closed")
      context stop self
  }

}