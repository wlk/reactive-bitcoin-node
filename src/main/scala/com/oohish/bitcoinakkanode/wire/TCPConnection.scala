package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.oohish.bitcoinscodec.structures.Message._
import java.io.ByteArrayInputStream
import scodec.bits.BitVector
import scodec.stream.{ decode, StreamDecoder }
import scalaz.stream.io
import scalaz.stream.Sink
import scalaz.concurrent.Task
import scodec.bits.ByteVector
import akka.io.Tcp
import akka.util.ByteString
import com.oohish.bitcoinakkanode.wire.MessageDecoder.DecodedMessage
import com.oohish.bitcoinakkanode.wire.MessageEncoder.EncodedMessage
import java.net.InetSocketAddress

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

  log.info("TCPConnection started...")

  val btcConnection = context.actorOf(BTCConnection.props(
    context.parent, remote, local, networkParams))
  context.watch(btcConnection)

  val decoder = context.actorOf(MessageDecoder.props(networkParams.packetMagic))
  val encoder = context.actorOf(MessageEncoder.props(networkParams.packetMagic))

  def receive = {
    case OutgoingMessage(msg) =>
      log.debug("received outgoing message: " + msg)
      encoder ! msg
    case EncodedMessage(b) =>
      log.debug("received encoded message: " + b)
      connection ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      log.debug("received decoded message: " + msg)
      btcConnection ! msg
    case Tcp.Received(data) =>
      log.debug("received tcp bytes: " + data)
      decoder ! Tcp.Received(data)
    case Tcp.CommandFailed(w: Tcp.Write) =>
      log.debug("write failed")
    case Terminated(btcConnection) =>
      log.debug("btc connection closed")
      connection ! Tcp.Close
    case "close" =>
      log.debug("closing connection")
      connection ! Tcp.Close
    case _: Tcp.ConnectionClosed =>
      log.debug("connection closed")
      context stop self
    case other =>
      log.debug("received other: " + other)
  }

}