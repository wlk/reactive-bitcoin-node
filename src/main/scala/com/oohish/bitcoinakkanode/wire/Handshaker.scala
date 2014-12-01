package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.math.BigInt.int2bigInt

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object Handshaker {
  def props(tcpConn: ActorRef, remote: InetSocketAddress,
    local: InetSocketAddress, networkParameters: NetworkParameters) =
    Props(classOf[Handshaker], tcpConn, remote, local, networkParameters)

  case class InitiateHandshake()
  case class FinishedHandshake()
  case class ConnectTimeout()

  val services = 1
  val userAgent = "/bitcoin-akka-node:0.1.0/"
  val height = 1
  val relay = true
}

class Handshaker(tcpConn: ActorRef, remote: InetSocketAddress,
  local: InetSocketAddress, networkParameters: NetworkParameters)
  extends Actor with ActorLogging {
  import Handshaker._

  val peerConn = context.actorOf(PeerConnection.props(tcpConn, networkParameters))

  def receive = ready

  def ready: Receive = {
    case InitiateHandshake() =>
      context.become(awaitingVersion)
      peerConn ! PeerConnection.OutgoingMessage(getVersion(remote, local))
    case v: Version =>
    // TODO
  }

  def awaitingVersion: Receive = {
    case v: Version =>
      context.become(awaitingVerack(v))
    case _: ConnectTimeout =>
      context.stop(self)
  }

  def awaitingVerack(v: Version): Receive = {
    case _: Verack =>
      finishHandshake(v)
    case _: ConnectTimeout =>
      context.stop(self)
  }

  def finishHandshake(v: Version): Unit = {
    peerConn ! PeerConnection.OutgoingMessage(Verack())
    context.parent ! FinishedHandshake()
    context.stop(self)
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