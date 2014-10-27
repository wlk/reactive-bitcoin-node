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

  case class GetVersion()
  case class SyncPeer(ref: ActorRef, v: Version)

}

trait Node extends NetworkParamsComponent with NetworkComponent {
  this: Actor with ActorLogging =>
  import context.dispatcher

  def nodeBehavior: Receive = {
    case Node.SyncPeer(ref, v) =>
      syncWithPeer(ref, v)
    case GetAddr() =>
      sender ! PeerConnection.Outgoing(Addr(List())) //TODO use real addrs
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
  }

  def syncWithPeer(peer: ActorRef, version: Version): Unit

}