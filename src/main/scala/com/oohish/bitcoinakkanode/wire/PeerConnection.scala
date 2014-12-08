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
import java.net.InetSocketAddress
import akka.io.Tcp.Register

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

object PeerConnection {
  def props(remote: InetSocketAddress,
    local: InetSocketAddress,
    tcpConn: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[PeerConnection], remote, local, tcpConn, networkParameters)

  val services = 1
  val userAgent = "/bitcoin-akka-node:0.1.0/"
  val height = 1
  val relay = true

  case class Connect()
  case class OutgoingMessage(msg: Message)
  case class OutgoingBytes(bytes: ByteString)
}

class PeerConnection(remote: InetSocketAddress,
  local: InetSocketAddress,
  tcpConn: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import PeerConnection._

  val translator = context.actorOf(MessageTranslator.props(tcpConn, self, networkParameters), "translator")
  val handshaker = context.actorOf(Handshaker.props(remote, local, networkParameters), "handshaker")
  val peerHandler: ActorRef = null

  tcpConn ! Register(translator)

  def receive = handshaking

  def handshaking: Receive = {
    case Connect() =>
      val version = getVersion(remote, local)
      handshaker ! version
    case msg: Message =>
      handshaker ! msg
    case OutgoingMessage(msg) =>
    // ignore
    case Handshaker.FinishedHandshake(v) =>
      context.become(connected(v))
  }

  def connected(version: Version): Receive = {
    case msg: Message =>
      peerHandler ! msg
    case OutgoingMessage(msg) =>
      translator ! MessageTranslator.OutgoingMessage(msg)
  }

  /*
   * Get the current Version network message.
   */
  def getVersion(remote: InetSocketAddress, local: InetSocketAddress) =
    Version(networkParameters.PROTOCOL_VERSION,
      services,
      Util.currentSeconds,
      NetworkAddress(services, remote),
      NetworkAddress(services, local),
      Util.genNonce,
      userAgent,
      height,
      relay)

}