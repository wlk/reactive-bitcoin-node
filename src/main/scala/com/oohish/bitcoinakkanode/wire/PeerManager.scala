package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.Array.canBuildFrom
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Try
import org.joda.time.DateTime
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.oohish.bitcoinakkanode.util.Util
import Util._
import com.oohish.bitcoinscodec.structures.NetworkAddress

object PeerManager {
  def props(networkParams: NetworkParameters) =
    Props(classOf[PeerManager], networkParams)

  def userAgent: String = "/bitcoin-akka-node:0.1.0/"
  def services = 1
  def height = 1
  def relay = true

  case class Connect()
  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress, v: Version)
  case class BroadCastMessage(msg: Message, exclude: List[ActorRef])
  case class AddAddress(addr: InetSocketAddress)
  case class GetPeers()
  case class GetRandomConnection()

  val peerLimit = 10
}

class PeerManager(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context._
  import PeerManager._

  var addresses = Set.empty[InetSocketAddress]
  var peers = Map.empty[ActorRef, (Long, NetworkAddress)]

  override def preStart() = {
    for (p <- dnsNodes) addresses += p
    system.scheduler.schedule(0 seconds, 1 second, self, Connect())
  }

  def receive: Receive = {
    case Connect() =>
      makeConnection()
    case GetVersion(remote, local) =>
      sender ! getVersion(remote, local)
    case AddAddress(addr) =>
      addresses += addr
    case PeerManager.BroadCastMessage(msg, exclude) =>
      broadcastToPeers(msg, exclude)
    case PeerManager.PeerConnected(ref, addr, v) =>
      val offset = v.timestamp - currentSeconds
      val networkAddress = NetworkAddress(v.services, addr)
      peers += ref -> (offset, networkAddress)
      context.watch(ref)
    case akka.actor.Terminated(ref) =>
      peers -= ref
    case GetPeers() =>
      sender ! peers.values.toList
    case GetRandomConnection() =>
      sender ! randomConnection()

  }

  /*
   * Attempt to establish an outgoing connection to another node.
   */
  def connectToPeer(address: InetSocketAddress) =
    context.actorOf(Client.props(address, networkParameters))

  /*
   * Get the network-adjusted time.
   */
  def networkTime = currentSeconds + medianOffset

  /*
   * Get the median offset from the local nodes clock time in seconds.
   */
  def medianOffset: Long = {
    if (peers.isEmpty) 0
    else {
      val offsets = peers.values.map(_._1).toList
      val medianIndex = offsets.length / 2
      offsets(medianIndex)
    }
  }

  /*
   * Get the set of addresses which are not connected.
   */
  def unconnectedAddresses =
    addresses.filter { addr =>
      !peers.values.exists {
        case (_, NetworkAddress(_, a)) =>
          a == addr
      }
    }

  /*
   * Attempt to establish a new connection from the list of saved addresses.
   */
  def makeConnection() = {
    if (peers.size < peerLimit) {
      util.Random.shuffle(unconnectedAddresses.toVector).take(1).foreach(connectToPeer)
    }
  }

  /*
   * Broadcast a message to all peers except for those excluded.
   */
  def broadcastToPeers(msg: Message, exclude: List[ActorRef]) =
    for (connection <- peers.keys if !(exclude contains connection))
      connection ! PeerConnection.Outgoing(msg)

  /*
   * Get the list of addresses of DNS nodes
   */
  def dnsNodes: List[InetSocketAddress] = for {
    fallback <- networkParameters.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParameters.port)

  /*
   * Get a random connection actor ref, if one exists.
   */
  def randomConnection() =
    if (peers.isEmpty)
      None
    else {
      val conns = peers.keys.toList
      Some(conns(scala.util.Random.nextInt(conns.length)))
    }

  /*
   * Get the current Version network message.
   */
  def getVersion(remote: InetSocketAddress, local: InetSocketAddress) =
    Version(networkParameters.PROTOCOL_VERSION,
      services,
      currentSeconds,
      NetworkAddress(services, remote),
      NetworkAddress(services, local),
      genNonce,
      userAgent,
      height,
      relay)

}