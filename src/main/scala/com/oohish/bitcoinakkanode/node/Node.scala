package com.oohish.bitcoinakkanode.node

import scala.BigInt
import scala.language.postfixOps

import org.joda.time.DateTime

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

  //case class SyncTimeout()

}

trait Node extends HasNetworkParams with NetworkComponent {
  this: Actor with ActorLogging =>
  import context.dispatcher

  def nodeBehavior: Receive = {
    case PeerManager.PeerConnected(ref, addr, version) =>
      syncWithPeer(ref, version)
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
    case GetAddr() => //TODO: store peer addr info
      val time = DateTime.now().getMillis()
      getPeerInfo
        .map(addrs => PeerConnection.Outgoing(
          Addr(addrs.map(addr => (time, NetworkAddress(BigInt(1), addr))))))
        .pipeTo(sender)
  }

  def syncWithPeer(peer: ActorRef, version: Version): Unit =
    peer ! PeerConnection.Outgoing(GetAddr())

}