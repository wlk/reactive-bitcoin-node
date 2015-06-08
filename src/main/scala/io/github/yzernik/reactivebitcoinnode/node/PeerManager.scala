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
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.PeerInfo

object PeerManager {
  def props(btc: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], btc, networkParameters)

  case class Initialize(ref: ActorRef)
  case object UpdateConnections
  case class AddNode(addr: InetSocketAddress, connect: Boolean)
  case class NewConnection(ref: ActorRef)
  case object GetNetworkTime
  case class ReceivedFromPeer(msg: Message, peer: ActorRef)
  case class SendToPeer(msg: Message, peer: ActorRef)
  case class SendToPeers(msg: Message, exclude: List[ActorRef])

  val NUM_CONNECTIONS = 10
}

class PeerManager(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.system
  import context.dispatcher
  import PeerManager._

  implicit val timeout = Timeout(8 seconds)

  var addresses: Set[InetSocketAddress] = getSeedAddresses.toSet
  var connections: Set[ActorRef] = Set.empty

  def receive = ready

  def ready: Receive = {
    case Initialize(ref) =>
      context.become(active(ref))
  }

  def active(listener: ActorRef): Receive = {
    case AddNode(addr, connect) =>
      addresses += addr
      if (connect)
        btc ! BTC.Connect(addr)
    case UpdateConnections =>
      log.info("updating connections...")
      updateConnections
    case BTC.Connected(version, inbound) =>
      val p = context.actorOf(PeerHandler.props(listener))
      context.watch(p)
      p ! PeerHandler.Initialize(sender)
      connections += p
    case Terminated(ref) =>
      connections -= ref
    case SendToPeer(msg, ref) =>
      log.info(s"Sending outgoing message: $msg")
      ref ! BTC.Send(msg)
    case GetNetworkTime =>
    /*
      sender ! getAverageNetworkTime
      * 
      */
    case Node.GetPeerInfo =>
      getPeerInfos.pipeTo(sender)
  }

  private def getRandomElement[A](s: Iterable[A]) =
    if (s.isEmpty) None
    else Some(s.toVector(Random.nextInt(s.size)))

  private def updateConnections =
    if (connections.size < NUM_CONNECTIONS)
      getRandomElement(addresses).foreach { addr =>
        self ! AddNode(addr, true)
      }

  private def getSeedAddresses =
    for {
      fallback <- networkParameters.dnsSeeds
      address <- Try(InetAddress.getAllByName(fallback)).getOrElse(Array())
    } yield new InetSocketAddress(address, networkParameters.port)

  private def getAverageNetworkTime =
    if (connections.isEmpty) 0
    else {
      ???
    }

  private def getPeerInfos: Future[Set[PeerInfo]] = {
    val fInfos = connections.map { ref =>
      (ref ? PeerHandler.GetInfo).mapTo[PeerInfo]
    }
    Future.sequence(fInfos)
  }

}