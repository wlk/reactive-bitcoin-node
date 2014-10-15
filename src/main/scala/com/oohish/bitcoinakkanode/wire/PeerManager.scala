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

object PeerManager {
  def props(networkParams: NetworkParameters) =
    Props(classOf[PeerManager], networkParams)

  def seedPeers(networkParams: NetworkParameters) = for {
    fallback <- networkParams.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParams.port)

  case class Connect()
  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress)
  case class ReceivedMessage(msg: Message, from: ActorRef)
  case class UnicastMessage(msg: Message, to: ActorRef)
  case class BroadCastMessage(msg: Message, exclude: List[ActorRef])
}

class PeerManager(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import com.oohish.bitcoinscodec.structures.Message._
  import context._
  import scala.language.postfixOps
  import scala.concurrent.duration._
  import PeerManager.Connect

  def dnsPeers = PeerManager.seedPeers(networkParams)

  var peers = Set.empty[InetSocketAddress]
  var connections = Map.empty[ActorRef, InetSocketAddress]
  val peerLimit = 10

  override def preStart() = {
    for (p <- dnsPeers) peers += p
    system.scheduler.schedule(0 seconds, 5 seconds, self, Connect())
  }

  def receive = {
    case Connect() =>
      if (connections.size < peerLimit) {
        val candidates = peers.filter(addr => !connections.values.exists(_ == addr))
        util.Random.shuffle(candidates.toVector).take(1).foreach(connectToPeer)
      }
    case PeerConnection.Incoming(msg) =>
      msgReceive(sender)(msg)
    case PeerManager.UnicastMessage(msg, to) =>
      to ! PeerConnection.Outgoing(msg)
    case PeerManager.BroadCastMessage(msg, exclude) =>
      for (connection <- connections.keys if !(exclude contains connection))
        connection ! PeerConnection.Outgoing(msg)
    case PeerManager.PeerConnected(ref, addr) =>
      log.debug("peer connected: {}", addr)
      connections += ref -> addr
      context.watch(ref)
      context.parent ! PeerManager.PeerConnected(ref, addr)
    case akka.actor.Terminated(ref) =>
      log.debug("peer disconnected: {}", connections(ref))
      connections -= ref
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Ping(nonce) =>
      sender ! PeerConnection.Outgoing(
        Pong(nonce))
    case Addr(addrs) =>
      for ((time, addr) <- addrs) peers += addr.address
    case other =>
      context.parent ! PeerManager.ReceivedMessage(other, sender)
  }

  def connectToPeer(address: InetSocketAddress) = {
    log.debug("connecting to: {}", address)
    context.actorOf(Client.props(address, networkParams))
  }

}