package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import scala.util.Try

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.btcio.actors.BTC

object PeerManager {
  def props(btc: ActorRef) =
    Props(classOf[PeerManager], btc)

  val seedUrls = List(
    "seed.bitcoin.sipa.be",
    "dnsseed.bluematt.me",
    "dnsseed.bitcoin.dashjr.org",
    "bitseed.xf2.org")

  case class Initialize(ref: ActorRef)
  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress)

  val NUM_CONNECTIONS = 10
}

class PeerManager(btc: ActorRef) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  import PeerManager._

  var addresses: Set[InetSocketAddress] = Set.empty
  var connections: Map[ActorRef, Version] = Map.empty

  def receive = ready

  def ready: Receive = {
    case Initialize(ref) =>
      addresses = getSeedAddresses.toSet
      context.system.scheduler.schedule(0 seconds, 5 seconds, self, UpdateConnections)
      context.become(active(ref))
  }

  def active(listener: ActorRef): Receive = {
    case AddNode(addr) =>
      log.info(s"connecting to addr: $addr")
      btc ! BTC.Connect(addr)
    case UpdateConnections =>
      log.info(s"updating connections...")
      updateConnections
      log.info(s"current connection count: ${connections.size}")
    case BTC.Connected(version) =>
      sender ! BTC.Register(self)
      connections += sender -> version
    case BTC.Closed =>
      connections -= sender
    case BTC.Received(msg) =>
      listener ! BTC.Received(msg)
    case o =>
      log.info(s"peer manager received other: $o")
  }

  private def getCandidateAddress = {
    val candidates = addresses
    if (candidates.isEmpty)
      None
    else
      Some(candidates.toVector(Random.nextInt(candidates.size)))
  }

  private def updateConnections =
    if (connections.size < NUM_CONNECTIONS) {
      val ca = getCandidateAddress
      ca.foreach { addr =>
        self ! AddNode(addr)
      }
    }

  private def getSeedAddresses =
    for {
      fallback <- seedUrls
      address <- Try(InetAddress.getAllByName(fallback)).getOrElse(Array())
    } yield new InetSocketAddress(address, 8333)

}