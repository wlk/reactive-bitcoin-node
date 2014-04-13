package com.oohish.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.Array.canBuildFrom
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import scala.util.Try

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object PeerManager {
  def props(node: ActorRef) =
    Props(classOf[PeerManager], node)

  val dnsSeeds = List(
    "seed.bitcoin.sipa.be",
    "dnsseed.bluematt.me",
    "dnsseed.bitcoin.dashjr.org",
    "bitseed.xf2.org")

  def seedPeers = for {
    fallback <- dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield Peer(new InetSocketAddress(address, 8333))

  case class Discovered(peers: List[Peer])
  case object CheckStatus
  case object GetConnectedPeers
  case class ConnectedPeers(connected: Map[ActorRef, Peer])
  case class PeerConnected(peer: Peer)
  case class PeerDisconnected(peer: Peer)

}

class PeerManager(node: ActorRef) extends Actor with ActorLogging {
  import PeerManager._
  import akka.actor.PoisonPill
  import akka.actor.Terminated
  import scala.concurrent.duration._
  import context.dispatcher
  import scala.language.postfixOps

  var maxConnections = 1
  var allPeers = Set.empty[Peer]
  var peerBlackList = Set.empty[Peer]
  var connectedPeers = Map.empty[ActorRef, Peer]
  def unconnectedPeers = allPeers.filterNot(p => connectedPeers.exists(kv => kv._2 == p))

  PeerManager.seedPeers.foreach { peer =>
    allPeers += peer
  }

  def randomUnconnected = Random.shuffle(unconnectedPeers.toList).headOption

  //self ! CheckStatus
  context.system.scheduler.schedule(0 milliseconds, 1000 milliseconds, self, CheckStatus)

  def receive = {
    case Discovered(peers) =>
      val notBlacklisted = peers.filterNot(peerBlackList.contains)
      allPeers ++= notBlacklisted
      log.info("discovered {} peers, now we know {} total peers", notBlacklisted.size, allPeers.size)

    case CheckStatus =>
      log.debug("received CheckStatus message. connectedPeers.size: " + connectedPeers.size)
      if (connectedPeers.size < maxConnections) {
        val toConnectOption: Option[Peer] = randomUnconnected
        toConnectOption.foreach { toConnect =>
          log.info("starting client...")
          val pc = context.actorOf(Client.props(toConnect, node))
        }
      } else if (connectedPeers.size > maxConnections) {
        connectedPeers.headOption foreach { kv =>
          kv._1 ! PoisonPill
        }
      }

    case GetConnectedPeers =>
      sender ! ConnectedPeers(connectedPeers)

    case PeerConnected(peer) => {
      val ref = sender
      context.watch(ref)
      log.info("peer connected: " + peer + " with actorRef: " + ref)
      connectedPeers += (ref -> peer)
      node ! PeerConnected(peer)
    }

    case Terminated(peerConnection) =>
      val peer = connectedPeers.get(peerConnection)
      peer foreach { p =>
        allPeers -= p
        peerBlackList += p
        connectedPeers -= peerConnection
        node ! PeerDisconnected(p)
      }

    case other => {
      log.warning("got other: " + other)
    }
  }

}