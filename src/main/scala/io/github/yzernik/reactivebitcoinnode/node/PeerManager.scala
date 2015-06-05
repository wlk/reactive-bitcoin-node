package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.annotation.migration
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
  def props(btc: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], btc, networkParameters)

  case class Initialize(ref: ActorRef)
  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress)

  val NUM_CONNECTIONS = 10
}

class PeerManager(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {
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
      log.info(s"current connection count: ${connections.size}")
      log.info(s"connecting to addr: $addr")
      btc ! BTC.Connect(addr)
    case UpdateConnections =>
      updateConnections
    case BTC.Connected(version) =>
      sender ! BTC.Register(self)
      connections += sender -> version
    case BTC.Closed =>
      connections -= sender
    case BTC.Received(msg) =>
      listener ! BTC.Received(msg)
    case Node.GetPeerInfo =>
      sender ! connections.values.toSet
    case o =>
      log.info(s"peer manager received other: $o")
  }

  private def getRandomElement[A](s: Iterable[A]) =
    if (s.isEmpty) None
    else Some(s.toVector(Random.nextInt(s.size)))

  private def updateConnections =
    if (connections.size < NUM_CONNECTIONS)
      getRandomElement(addresses).foreach { addr =>
        self ! AddNode(addr)
      }

  private def getSeedAddresses =
    for {
      fallback <- networkParameters.dnsSeeds
      address <- Try(InetAddress.getAllByName(fallback)).getOrElse(Array())
    } yield new InetSocketAddress(address, networkParameters.port)

}