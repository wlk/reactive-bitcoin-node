package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import scala.util.Try

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.BTC.PeerInfo

object PeerManager {
  def props(btc: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], btc, networkParameters)

  case class Initialize(blockchainController: ActorRef)
  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress, connect: Boolean)
  case class NewConnection(ref: ActorRef)
  case object GetNetworkTime
  case object GetAddresses
  case class ReceivedFromPeer(msg: Message, peer: ActorRef)
  case class SendToPeer(msg: Message, peer: ActorRef)
  case class SendToPeers(msg: Message, exclude: List[ActorRef])

  val NUM_CONNECTIONS = 10

}

class PeerManager(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  import PeerManager._

  implicit val timeout = Timeout(10 seconds)

  var addresses: Set[InetSocketAddress] = getSeedAddresses(networkParameters.dnsSeeds, networkParameters.port).toSet
  var connections: Map[ActorRef, Version] = Map.empty

  def receive = ready

  def ready: Receive = {
    case Initialize(blockchainController) =>
      context.become(active(blockchainController))
  }

  def active(blockchainController: ActorRef): Receive = {
    case AddNode(addr, connect) =>
      addNode(addr, connect)
    case UpdateConnections =>
      updateConnections
    case BTC.Connected(version, event, inbound) =>
      registerConnection(blockchainController, sender, version, inbound)
    case Terminated(ref) =>
      connections -= ref
    case GetNetworkTime =>
      sender ! getAverageNetworkTime
    case Node.GetPeerInfo =>
      getPeerInfos.pipeTo(sender)
    case GetAddresses =>
      sender ! addresses.toList
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
    val handler = context.actorOf(PeerHandler.props(blockchainController, self, networkParameters))
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
   * Get the average network time of the connected peers.
   */
  private def getAverageNetworkTime =
    if (connections.isEmpty) 0
    else {
      val times = connections.values.map(_.timestamp)
      times.sum / times.size
    }

  /**
   * Get the peer info from each of the connected peers.
   */
  private def getPeerInfos = {
    val fInfos = connections.keys.map { ref =>
      (ref ? BTC.GetPeerInfo).mapTo[PeerInfo]
    }
    Future.sequence(fInfos.toSet)
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