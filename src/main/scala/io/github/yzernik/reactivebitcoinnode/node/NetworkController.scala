package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetSocketAddress

import scala.BigInt
import scala.annotation.migration
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.messages.GetAddr
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress

object NetworkController {
  def props(blockchain: ActorRef, peerManager: ActorRef, btc: ActorRef) =
    Props(classOf[NetworkController], blockchain, peerManager, btc)

  case object Initialize
}

class NetworkController(blockchain: ActorRef, peerManager: ActorRef, btc: ActorRef)
  extends Actor with ActorLogging {
  import NetworkController._
  import context.system
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  var preferredDownloadPeers: Vector[ActorRef] = Vector.empty

  def receive = ready

  def ready: Receive = {
    case Initialize =>
      peerManager ! PeerManager.Initialize(self)
      context.system.scheduler.schedule(0 seconds, 1 seconds, peerManager, PeerManager.UpdateConnections)
      context.become(active(true))
  }

  def active(syncing: Boolean): Receive = {
    case PeerManager.NewConnection(ref) =>
      log.info(s"Got a new connection: $ref")
      handleNewConnection(ref)
    case PeerManager.ReceivedFromPeer(msg, ref) =>
      log.info(s"network controller received from $ref other: $msg")
      handlePeerMessage(ref, syncing)(msg)
    case o =>
      log.info(s"network controller received other: $o")
  }

  private def getNetworkTime: Future[Long] = {
    (peerManager ? PeerManager.GetNetworkTime).mapTo[Long]
  }

  private def getAddresses: Future[Set[InetSocketAddress]] = {
    (peerManager ? PeerManager.GetAddresses).mapTo[Set[InetSocketAddress]]
  }

  private def handleNewConnection(peer: ActorRef) = {
    peerManager ! PeerManager.SendToPeer(GetAddr(), peer)
    context.become(active(true))
  }

  private def getAddr =
    for {
      netAddrs <- getAddresses.map(addrs => addrs.map(NetworkAddress(BigInt(1L), _)))
      t <- getNetworkTime
    } yield Addr(netAddrs.toList.map(addr => (t, addr)))

  private def handleAddr(addrs: List[(Long, NetworkAddress)]) =
    addrs.map { case (t, a) => a.address }
      .foreach { addr =>
        peerManager ! PeerManager.AddNode(addr, false)
      }

  private def sendToPeer(peer: ActorRef, futureMsg: Future[Message]) = {
    val cmd = futureMsg.map { msg =>
      println(s"sending msg to peer: $msg")
      PeerManager.SendToPeer(msg, peer)
    }
    cmd.pipeTo(peerManager)
  }

  def handlePeerMessage(peer: ActorRef, syncing: Boolean): PartialFunction[Message, Unit] = {
    case Addr(addrs) =>
      handleAddr(addrs)
    case GetAddr() =>
      sendToPeer(peer, getAddr)
    case other =>
    // println(s"received msg: $other")
  }

}