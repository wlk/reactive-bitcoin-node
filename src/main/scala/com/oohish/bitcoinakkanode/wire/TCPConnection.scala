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

object TCPConnection {
  def props(manager: ActorRef, connection: ActorRef) =
    Props(classOf[TCPConnection], manager, connection)

  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class TCPConnection(manager: ActorRef, connection: ActorRef) extends Actor with ActorLogging {
  import TCPConnection._
  import akka.actor.Terminated

  log.info("TCPConnection started...")

  val btcConnection = context.actorOf(BTCConnection.props(context.parent))
  context.watch(btcConnection)

  val decoder = context.actorOf(MessageDecoder.props(0L))
  val encoder = context.actorOf(MessageEncoder.props(0L))

  def receive = {
    case OutgoingMessage(msg) =>
      log.info("received outgoing message: " + msg)
      encoder ! msg
    case EncodedMessage(b) =>
      log.info("received encoded message: " + b)
      log.info("received encoded message w/ length: " + b.length)
      connection ! Tcp.Write(b)
    case DecodedMessage(msg) =>
      log.info("received decoded message: " + msg)
      btcConnection ! msg
    case Tcp.Received(data) =>
      log.info("received tcp bytes: " + data)
      decoder ! Tcp.Received(data)
    case Tcp.CommandFailed(w: Tcp.Write) =>
      log.info("write failed")
    case Terminated(btcConnection) =>
      log.info("btc connection closed")
      connection ! Tcp.Close
    case "close" =>
      log.info("closing connection")
      connection ! Tcp.Close
    case _: Tcp.ConnectionClosed =>
      log.info("connection closed")
      context stop self
    case other =>
      log.info("received other: " + other)
  }

}