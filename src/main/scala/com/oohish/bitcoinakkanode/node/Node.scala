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
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object Node {

  //case class SyncTimeout()

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

trait Node extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.node.Node._
  import context.dispatcher
  import com.oohish.bitcoinscodec.structures._

  implicit val timeout = Timeout(5 seconds)

  def networkParams: NetworkParameters

  val pm = context.actorOf(PeerManager.props(self, networkParams))

  def receive: Receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      onPeerConnected(ref)
    case msg: Message =>
      receiveMessage(msg)
    case cmd: APICommand =>
      receiveAPICommand(cmd)
  }

  def onPeerConnected(peer: ActorRef): Unit =
    peer ! PeerConnection.Outgoing(GetAddr())

  def receiveMessage: PartialFunction[Message, Unit] = {
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        pm ! PeerManager.AddPeer(addr.address)
    case GetAddr() => //TODO: store peer addr info
      val time = DateTime.now().getMillis()
      getPeerInfo
        .map(addrs => PeerConnection.Outgoing(
          Addr(addrs.map(addr => (time, NetworkAddress(BigInt(1), addr))))))
        .pipeTo(sender)
    case _ =>
  }

  def receiveAPICommand: PartialFunction[APICommand, Unit] = {
    case GetConnectionCount() =>
      getConnectionCount
        .pipeTo(sender)
    case GetPeerInfo() =>
      getPeerInfo
        .pipeTo(sender)
    case _ =>
      sender ! "Command not implemented for this node."
  }

  def getConnectionCount(): Future[Int] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]
      .map(_.length)

  def getPeerInfo(): Future[List[InetSocketAddress]] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]

}