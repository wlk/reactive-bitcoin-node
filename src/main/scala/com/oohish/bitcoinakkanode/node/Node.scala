package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.language.postfixOps

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages._
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.pipe

object Node {
  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class SyncPeer(ref: ActorRef, v: Version)
}

trait Node
  extends NetworkParamsComponent
  with NetworkComponent
  with APIClient {
  this: Actor with ActorLogging =>
  import context.dispatcher

  def nodeBehavior: Receive
  def syncWithPeer(peer: ActorRef, version: Version): Unit
  def getBlockChainHeight(): Future[Int]
  def services: BigInt
  def relay: Boolean

  val userAgent: String = "/bitcoin-akka-node:0.1.0/"

  def networkBehavior: Receive = {
    case Node.SyncPeer(ref, v) =>
      syncWithPeer(ref, v)
    case Node.GetVersion(remote, local) =>
      getVersion(remote, local)
        .pipeTo(sender)
    case Ping(nonce) =>
      sender ! PeerConnection.Outgoing(Pong(nonce))
    case GetAddr() =>
      sender ! PeerConnection.Outgoing(Addr(List())) //TODO use real addrs
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Future[Version] =
    getBlockChainHeight
      .map(blockStart =>
        Version(
          networkParams.PROTOCOL_VERSION,
          services,
          DateTime.now().getMillis() / 1000,
          NetworkAddress(services, remote),
          NetworkAddress(services, local),
          Util.genNonce,
          userAgent,
          blockStart,
          relay))

  def receive: Receive =
    networkBehavior orElse nodeBehavior orElse apiClientBehavior

}