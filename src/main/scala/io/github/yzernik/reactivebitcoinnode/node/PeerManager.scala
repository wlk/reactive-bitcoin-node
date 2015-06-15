package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.annotation.migration
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

  case class Initialize(ref: ActorRef)
  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress, connect: Boolean)
  case class NewConnection(ref: ActorRef)
  case object GetNetworkTime
  case object GetAddresses
  case class ReceivedFromPeer(msg: Message, peer: ActorRef)
  case class SendToPeer(msg: Message, peer: ActorRef)
  case class SendToPeers(msg: Message, exclude: List[ActorRef])

  val NUM_CONNECTIONS = 10

  private def getSeedAddresses(seeds: List[String], port: Int) =
    for {
      fallback <- seeds
      address <- Try(InetAddress.getAllByName(fallback)).getOrElse(Array())
    } yield new InetSocketAddress(address, port)
}

class PeerManager(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  import PeerManager._

  implicit val timeout = Timeout(8 seconds)

  var addresses: Set[InetSocketAddress] = getSeedAddresses(networkParameters.dnsSeeds, networkParameters.port).toSet
  var connections: Map[ActorRef, Version] = Map.empty

  def receive = ready

  def ready: Receive = {
    case Initialize(ref) =>
      context.become(active(ref))
  }

  def active(listener: ActorRef): Receive = {
    case AddNode(addr, connect) =>
      log.info(s"adding connection to : $addr, connecting?: $connect")
      addNode(addr, connect)
    case UpdateConnections =>
      log.info("updating connections...")
      updateConnections
    case BTC.Connected(version) =>
      registerConnection(listener, sender, version)
    case BTC.Received(msg) =>
      log.info(s"Recevied incoming message: $msg")
      listener ! PeerManager.ReceivedFromPeer(msg, sender)
    case SendToPeer(msg, ref) =>
      log.info(s"Sending outgoing message: $msg to peer: $ref")
      ref ! BTC.Send(msg)
    case Terminated(ref) =>
      connections -= ref
    case GetNetworkTime =>
      sender ! getAverageNetworkTime
    case Node.GetPeerInfo =>
      getPeerInfos.pipeTo(sender)
    case GetAddresses =>
      sender ! addresses
  }

  private def addNode(addr: InetSocketAddress, connect: Boolean) = {
    addresses += addr
    if (connect) btc ! BTC.Connect(addr)
  }

  private def registerConnection(listener: ActorRef, conn: ActorRef, v: Version) = {
    context.watch(conn)
    conn ! BTC.Register(self)
    listener ! PeerManager.NewConnection(conn)
    connections += conn -> v
  }

  private def getRandomElement[A](s: Iterable[A]) =
    if (s.isEmpty) None
    else Some(s.toVector(Random.nextInt(s.size)))

  private def updateConnections =
    if (connections.size < NUM_CONNECTIONS)
      getRandomElement(addresses).foreach { addr =>
        self ! AddNode(addr, true)
      }

  private def getAverageNetworkTime =
    if (connections.isEmpty) 0
    else {
      val times = connections.values.map(_.timestamp)
      times.sum / times.size
    }

  private def getPeerInfos = {
    val fInfos = connections.keys.map { ref =>
      (ref ? BTC.GetPeerInfo).mapTo[PeerInfo]
    }
    Future.sequence(fInfos.toSet)
  }

}