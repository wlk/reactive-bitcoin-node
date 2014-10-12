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

object PeerManager {
  def props(networkParams: NetworkParameters) =
    Props(classOf[PeerManager], networkParams)

  def seedPeers(networkParams: NetworkParameters) = for {
    fallback <- networkParams.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParams.port)

  case class PeerConnected(ref: ActorRef)
  case class ReceivedMessage(msg: Message, from: ActorRef)
  case class UnicastMessage(msg: Message, to: ActorRef)
  case class BroadCastMessage(msg: Message, exclude: List[ActorRef])
}

class PeerManager(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import com.oohish.bitcoinscodec.structures.Message._

  def dnsPeers = PeerManager.seedPeers(networkParams)
  val pc = context.actorOf(Client.props(dnsPeers.head, networkParams))

  def receive = {
    case PeerConnection.Incoming(msg) =>
      log.info("peer manager received {} from {}", msg.getClass(), sender)
      context.parent ! PeerManager.ReceivedMessage(msg, sender)
    case PeerManager.UnicastMessage(msg, to) =>
      log.debug("peer manager sending {} to {}", msg.getClass(), to)
      to ! PeerConnection.Outgoing(msg)
    case PeerManager.PeerConnected(ref) =>
      context.parent ! PeerManager.PeerConnected(ref)
  }

}