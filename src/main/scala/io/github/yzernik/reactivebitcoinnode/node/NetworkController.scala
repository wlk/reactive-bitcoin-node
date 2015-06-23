package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetSocketAddress

import scala.BigInt
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
import io.github.yzernik.bitcoinscodec.messages.GetHeaders
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress

object NetworkController {
  def props(peerManager: ActorRef, blockchainController: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[NetworkController], peerManager, blockchainController, networkParameters)

  case object Initialize
}

class NetworkController(peerManager: ActorRef, blockchainController: ActorRef, networkParameters: NetworkParameters)
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
      handleNewConnection(ref, syncing)
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

  private def getBlockLocatorHashes: Future[List[Hash]] = {
    (blockchainController ? BlockchainController.GetBlockLocator).mapTo[List[Hash]]
  }

  private def handleNewConnection(peer: ActorRef, syncing: Boolean) = {
    peerManager ! PeerManager.SendToPeer(GetAddr(), peer)
    if (syncing) {
      val msg = getBlockLocatorHashes.map { bl =>
        GetHeaders(networkParameters.PROTOCOL_VERSION, bl, Hash.NULL)
      }
      sendToPeer(peer, msg)
    }

  }

  /**
   * Get the Addr message to send to a peer.
   */
  private def getAddr =
    for {
      netAddrs <- getAddresses.map(addrs => addrs.map(NetworkAddress(BigInt(1L), _)))
      t <- getNetworkTime
    } yield Addr(netAddrs.toList.map(addr => (t, addr)))

  /**
   * Handle an Addr message from a peer.
   */
  private def handleAddr(addrs: List[(Long, NetworkAddress)]) =
    addrs.map { case (t, a) => a.address }
      .foreach { addr =>
        peerManager ! PeerManager.AddNode(addr, false)
      }

  /**
   * Send a message to a specific peer.
   */
  private def sendToPeer(peer: ActorRef, futureMsg: Future[Message]) = {
    val cmd = futureMsg.map { msg =>
      PeerManager.SendToPeer(msg, peer)
    }
    cmd.pipeTo(peerManager)
  }

  /**
   * Handle a message from a peer.
   */
  def handlePeerMessage(peer: ActorRef, syncing: Boolean): PartialFunction[Message, Unit] = {
    case Addr(addrs) =>
      handleAddr(addrs)
    case GetAddr() =>
      sendToPeer(peer, getAddr)
    case other =>
    // println(s"received msg: $other")
  }

}