package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.language.postfixOps

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.GetAddr
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

trait Node extends NetworkParamsComponent with NetworkComponent {
  this: Actor with ActorLogging =>
  import context.dispatcher

  def nodeBehavior: Receive = {
    case Node.SyncPeer(ref, v) =>
      syncWithPeer(ref, v)
    case Node.GetVersion(remote, local) =>
      getVersion(remote, local)
        .pipeTo(sender)
    case GetAddr() =>
      sender ! PeerConnection.Outgoing(Addr(List())) //TODO use real addrs
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
  }

  def syncWithPeer(peer: ActorRef, version: Version): Unit

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Future[Version] =
    getBlockStart
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
          true))

  def getBlockStart(): Future[Int]
  def services: BigInt
  def userAgent: String = "/Satoshi:0.7.2/"

}