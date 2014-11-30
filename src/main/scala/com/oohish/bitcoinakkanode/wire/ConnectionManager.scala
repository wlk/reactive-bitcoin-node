package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object ConnectionManager {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ConnectionManager], networkParams)

  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress, v: Version)
  case class GetPeers()

  val peerLimit = 10
}

class ConnectionManager extends Actor with ActorLogging {
  import ConnectionManager._

  var peers = Map.empty[ActorRef, (Long, NetworkAddress)]

  def receive: Receive = {
    case PeerConnected(ref, addr, v) =>
      val offset = v.timestamp - Util.currentSeconds
      val networkAddress = NetworkAddress(v.services, addr)
      peers += ref -> (offset, networkAddress)
      context.watch(ref)
    case akka.actor.Terminated(ref) =>
      peers -= ref
    case GetPeers() =>
      sender ! peers.values.toList.map(_._2)
  }

}