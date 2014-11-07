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
  import context._
  import PeerManager._

  var addresses = Set.empty[InetSocketAddress]
  var peers = Map.empty[ActorRef, (Long, InetSocketAddress)]
  val peerLimit = 10

  override def preStart() = {
    for (p <- dnsNodes) addresses += p
    system.scheduler.schedule(0 seconds, 1 second, self, Connect())
  }

  def receive: Receive = {
    case Connect() =>
      makeConnection()
    case AddPeer(addr) =>
      addresses += addr
    case PeerManager.BroadCastMessage(msg, exclude) =>
      for (connection <- peers.keys if !(exclude contains connection))
        connection ! PeerConnection.Outgoing(msg)
    case PeerManager.PeerConnected(ref, addr, v) =>
      val offset = v.timestamp - currentSeconds
      peers += ref -> (offset, addr)
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
    context.actorOf(Client.props(node, address, networkParams))

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
   * Attempt to establish a new connection from the list of saved addresses.
   */
  def makeConnection() = {
    if (peers.size < peerLimit) {
      val candidates = addresses.filter(addr => !peers.values.exists(_._2 == addr))
      util.Random.shuffle(candidates.toVector).take(1).foreach(connectToPeer)
    }
  }

  /*
   * Get the list of addresses of DNS nodes
   */
  def dnsNodes: List[InetSocketAddress] = for {
    fallback <- networkParams.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParams.port)

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
}