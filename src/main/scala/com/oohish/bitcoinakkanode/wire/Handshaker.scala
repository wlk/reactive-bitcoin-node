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
  def props(peerConnection: ActorRef, remote: InetSocketAddress,
    local: InetSocketAddress, networkParameters: NetworkParameters) =
    Props(classOf[Handshaker], peerConnection, remote, local, networkParameters)

  case class InitiateHandshake()
  case class FinishedHandshake(ref: ActorRef, version: Version)
  case class ConnectTimeout()

  val services = 1
  val userAgent = "/bitcoin-akka-node:0.1.0/"
  val height = 1
  val relay = true
}

class Handshaker(peerConnection: ActorRef, remote: InetSocketAddress,
  local: InetSocketAddress, networkParameters: NetworkParameters)
  extends Actor with ActorLogging {
  import Handshaker._

  peerConnection ! PeerConnection.Register(self)

  def receive = ready

  def ready: Receive = {
    case InitiateHandshake() =>
      context.become(awaitingVersion)
      peerConnection ! PeerConnection.OutgoingMessage(getVersion(remote, local))
    case v: Version =>
    // TODO
    case _: ConnectTimeout =>
      context.stop(self)
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
    peerConnection ! PeerConnection.OutgoingMessage(Verack())
    context.parent ! FinishedHandshake(peerConnection, v)
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