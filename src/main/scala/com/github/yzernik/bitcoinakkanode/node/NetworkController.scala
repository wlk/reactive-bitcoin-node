package com.github.yzernik.bitcoinakkanode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.math.BigInt.int2bigInt

import com.github.yzernik.btcio.akka.BTC
import com.github.yzernik.bitcoinscodec.messages.Version
import com.github.yzernik.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO

object NetworkController {
  def props() =
    Props(classOf[NetworkController])
}

class NetworkController extends Actor with ActorLogging {
  import context.system

  val versionResource = context.actorOf(VersionResource.props)
  val magic = MainNetParams.packetMagic
  val manager = IO(new BTC(magic))
  //manager ! BTC.Bind(8334)
  manager ! BTC.Connect(new InetSocketAddress(InetAddress.getByName("cpe-104-34-16-248.socal.res.rr.com"), 8333))

  def receive = {
    case BTC.Connected(remote, local, _) =>
      log.info(s"connected to: $remote")
      val handler = context.actorOf(PeerHandler.props(versionResource))
      handler ! PeerHandler.HandlePeer(sender, remote, local)
    case other =>
      log.info(s"receive other: $other")
  }

}

object PeerHandler {
  def props(versionResource: ActorRef) =
    Props(classOf[PeerHandler], versionResource)

  case class HandlePeer(peer: ActorRef, remote: InetSocketAddress, local: InetSocketAddress)
}

class PeerHandler(versionResource: ActorRef)
  extends Actor with ActorLogging {
  import PeerHandler._

  def receive = ready

  def ready: Receive = {
    case HandlePeer(peer, remote, local) =>
      peer ! BTC.Register(self)
  }

  def handling(peer: ActorRef): Receive = {
    case x =>
      log.info(s"received: $x")
  }

}

object VersionResource {
  def props = Props(classOf[VersionResource])

  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class GotVersion(version: Version)
}

class VersionResource extends Actor with ActorLogging {
  import VersionResource._

  def receive = {
    case GetVersion(remote, local) =>
      val v = getVersion(remote, local)
      sender ! GotVersion(v)
  }

  /**
   * Get the local node's Version message.
   */
  def getVersion(remote: InetSocketAddress, local: InetSocketAddress) =
    Version(0, 0, 0, NetworkAddress(1, remote), NetworkAddress(1, local), 0, "", 0, false)

}


