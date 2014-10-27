package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.Array.canBuildFrom
import scala.language.postfixOps
import scala.util.Try
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.ActorRef
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.messages._
import com.oohish.bitcoinakkanode.node.Node

object PeerManager {
  def props(node: ActorRef,
    networkParams: NetworkParameters) =
    Props(classOf[PeerManager], node, networkParams)

  case class Connect()
  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress, v: Version)
  case class BroadCastMessage(msg: Message, exclude: List[ActorRef])
  case class AddPeer(addr: InetSocketAddress)
  case class GetPeers()
  case class GetRandomConnection()
}

class PeerManager(node: ActorRef,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import com.oohish.bitcoinscodec.structures.Message._
  import context._
  import scala.language.postfixOps
  import scala.concurrent.duration._
  import PeerManager._

  def dnsPeers = for {
    fallback <- networkParams.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParams.port)

  var peers = Set.empty[InetSocketAddress]
  var connections = Map.empty[ActorRef, InetSocketAddress]
  val peerLimit = 10

  override def preStart() = {
    for (p <- dnsPeers) peers += p
    system.scheduler.schedule(0 seconds, 1 second, self, Connect())
  }

  def receive = {
    case Connect() =>
      if (connections.size < peerLimit) {
        val candidates = peers.filter(addr => !connections.values.exists(_ == addr))
        util.Random.shuffle(candidates.toVector).take(1).foreach(connectToPeer)
      }
    case AddPeer(addr) =>
      peers += addr
    case PeerManager.BroadCastMessage(msg, exclude) =>
      for (connection <- connections.keys if !(exclude contains connection))
        connection ! PeerConnection.Outgoing(msg)
    case PeerManager.PeerConnected(ref, addr, v) =>
      log.debug("peer connected: {}", addr)
      connections += ref -> addr
      context.watch(ref)
      ref ! PeerConnection.Outgoing(GetAddr())
      node ! Node.SyncPeer(ref, v)
    case akka.actor.Terminated(ref) =>
      log.debug("peer disconnected: {}", connections(ref))
      connections -= ref
    case GetPeers() =>
      sender ! connections.values.toList
    case GetRandomConnection() =>
      val conns = connections.keys.toList
      val randConn = if (conns.length > 0)
        Some(conns(scala.util.Random.nextInt(conns.length)))
      else
        None
      sender ! randConn
  }

  def connectToPeer(address: InetSocketAddress) =
    context.actorOf(Client.props(node, address, networkParams))

}