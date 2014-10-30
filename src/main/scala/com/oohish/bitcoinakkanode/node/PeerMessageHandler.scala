package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinakkanode.wire.NetworkParameters

import org.joda.time.DateTime
import com.oohish.bitcoinscodec.structures.NetworkAddress
import com.oohish.bitcoinakkanode.util.Util

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object PeerMessageHandler {
  case class GotMessage(msg: Message)
  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class PeerConnected(ref: ActorRef)
}

trait PeerMessageHandler extends Actor {
  import PeerMessageHandler._

  def receive: Receive = {
    case GotMessage(msg) =>
      handlePeerMessage(msg)
    case GetVersion(remote, local) =>
      sender ! getVersion(remote, local)
    case PeerConnected(ref) =>
      onPeerConnected(ref)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Version =
    Version(networkParams.PROTOCOL_VERSION,
      services,
      now,
      NetworkAddress(services, remote),
      NetworkAddress(services, local),
      Util.genNonce,
      Node.userAgent,
      height,
      relay)

  def now = DateTime.now().getMillis() / 1000

  def networkParams: NetworkParameters
  def services: BigInt
  def height: Int
  def relay: Boolean
  def handlePeerMessage(msg: Message): Unit
  def onPeerConnected(ref: ActorRef): Unit

}