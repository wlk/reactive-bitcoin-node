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
import com.oohish.bitcoinscodec.structures.Message.Message
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
  var connections = Map.empty[InetSocketAddress, ActorRef]
  val peerLimit = 10

  override def preStart() = {
    for (p <- dnsPeers) peers += p
    system.scheduler.schedule(0 seconds, 5 seconds, self, Connect())
  }

  def receive = {
    case Connect() =>
      if (connections.size < peerLimit) {
        log.info("trying to connect....")
        val candidates = peers.filter(!connections.contains(_))
        log.info("num candidates: {}", candidates.size)
        util.Random.shuffle(candidates.toVector).take(1).foreach(connectToPeer)
      }
    case PeerConnection.Incoming(msg) =>
      log.info("peer manager received {} from {}", msg.getClass(), sender)
      msgReceive(sender)(msg)
    case PeerManager.UnicastMessage(msg, to) =>
      log.debug("peer manager sending {} to {}", msg.getClass(), to)
      to ! PeerConnection.Outgoing(msg)
    case PeerManager.BroadCastMessage(msg, exclude) =>
      for (connection <- connections.values if !(exclude contains connection)) {
        log.debug("peer manager sending {} to {}", msg.getClass(), connection)
        connection ! PeerConnection.Outgoing(msg)
      }
    case PeerManager.PeerConnected(ref, addr) =>
      connections += addr -> ref
      log.info("peer connected. num connections: {}", connections.size)
      context.parent ! PeerManager.PeerConnected(ref, addr)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Ping(nonce) =>
      sender ! PeerConnection.Outgoing(
        Pong(nonce))
    case Addr(addrs) =>
      log.info("addr list size: {}", addrs.size)
      for ((time, addr) <- addrs) peers += addr.address
    case other =>
      log.debug("node received other message: {}", other.getClass())
      context.parent ! PeerManager.ReceivedMessage(other, sender)
  }

  def connectToPeer(address: InetSocketAddress) = {
    log.info("connecting to: {}", address)
    context.actorOf(Client.props(address, networkParams))
  }

}