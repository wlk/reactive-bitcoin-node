package io.github.yzernik.reactivebitcoinnode.network

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import scala.util.Try

import PeerManager.AddNode
import PeerManager.GetConnections
import PeerManager.NUM_CONNECTIONS
import PeerManager.UpdateConnections
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.reactivebitcoinnode.node.NetworkParameters

object PeerManager {
  def props(blockchainController: ActorRef, btc: ActorRef, blockDownloader: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], blockchainController, btc, blockDownloader, networkParameters)

  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress, connect: Boolean)
  case object GetConnections

  val NUM_CONNECTIONS = 10

}

class PeerManager(blockchainController: ActorRef, btc: ActorRef, blockDownloader: ActorRef,
                  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  import PeerManager._

  implicit val timeout = Timeout(10 seconds)

  var addresses: Set[InetSocketAddress] = getSeedAddresses(networkParameters.dnsSeeds, networkParameters.port).toSet
  var connections: Map[ActorRef, Version] = Map.empty

  def receive = {
    case AddNode(addr, connect) =>
      addNode(addr, connect)
    case UpdateConnections =>
      updateConnections
    case BTC.Connected(version, event, inbound) =>
      registerConnection(blockchainController, sender, version, inbound)
    case Terminated(ref) =>
      connections -= ref
    case GetConnections =>
      sender ! connections
  }

  /**
   * Add a new network address, and potentially connect to it.
   */
  private def addNode(addr: InetSocketAddress, connect: Boolean) = {
    addresses += addr
    if (connect) btc ! BTC.Connect(addr)
  }

  private def registerConnection(blockchainController: ActorRef, conn: ActorRef, v: Version, inbound: Boolean) = {
    context.watch(conn)
    val handler = context.actorOf(PeerHandler.props(blockchainController, self, blockDownloader))
    handler ! PeerHandler.Initialize(conn, inbound)
    connections += conn -> v
  }

  /**
   * Get a random element from an iterable.
   */
  private def getRandomElement[A](s: Iterable[A]) =
    if (s.isEmpty) None
    else Some(s.toVector(Random.nextInt(s.size)))

  /**
   * Update the current connections.
   */
  private def updateConnections =
    if (connections.size < NUM_CONNECTIONS)
      getRandomElement(addresses).foreach { addr =>
        self ! AddNode(addr, true)
      }

  /**
   * Get the list of DNS seed addresses.
   */
  private def getSeedAddresses(seeds: List[String], port: Int) =
    for {
      fallback <- seeds
      address <- Try(InetAddress.getAllByName(fallback)).getOrElse(Array())
    } yield new InetSocketAddress(address, port)

}