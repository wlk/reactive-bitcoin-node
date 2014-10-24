package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress
import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import org.joda.time.DateTime
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.structures.NetworkAddress
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import com.oohish.bitcoinscodec.messages.Inv
import com.oohish.bitcoinscodec.messages.Version

object Node {

  //case class SyncTimeout()

}

trait Node extends HasNetworkParams {
  this: Actor with ActorLogging =>

  import com.oohish.bitcoinakkanode.node.Node._
  import context.dispatcher
  import com.oohish.bitcoinscodec.structures._

  implicit val timeout = Timeout(1 second)

  lazy val pm = context.actorOf(PeerManager.props(self, networkParams))

  def nodeBehavior: Receive = {
    case PeerManager.PeerConnected(ref, addr, version) =>
      syncWithPeer(ref, version)
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
    case GetAddr() => //TODO: store peer addr info
      val time = DateTime.now().getMillis()
      getPeerInfo
        .map(addrs => PeerConnection.Outgoing(
          Addr(addrs.map(addr => (time, NetworkAddress(BigInt(1), addr))))))
        .pipeTo(sender)
  }

  def syncWithPeer(peer: ActorRef, version: Version): Unit =
    peer ! PeerConnection.Outgoing(GetAddr())

  def getConnectionCount(): Future[Int] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]
      .map(_.length)

  def getPeerInfo(): Future[List[InetSocketAddress]] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]

}