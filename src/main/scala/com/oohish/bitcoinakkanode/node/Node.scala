package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout.durationToTimeout

object Node {
  def props(networkParameters: NetworkParameters) =
    Props(classOf[Node], networkParameters)

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

class Node(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import Node._

  val peerManager: ActorRef = context.actorOf(PeerManager.props(networkParameters), "peer-manager")
  val networkListener: ActorRef = context.actorOf(NetworkListener.props(null, peerManager))

  peerManager ! PeerManager.RegisterListener(networkListener)

  def receive: Receive = {
    case GetConnectionCount() =>
      getConnectionCount().pipeTo(sender)
    case GetPeerInfo() =>
      getPeerInfo().pipeTo(sender)
    case GetBestBlockHash() =>
      getBestBlockHash.pipeTo(sender)
    case GetBlockCount() =>
      getBlockCount().pipeTo(sender)
    case GetBlockHash(index) =>
      getBlockHash(index).pipeTo(sender)
    case cmd: APICommand =>
      sender ! "Command not found."
  }

  private def getConnectionCount(): Future[Int] = {
    (peerManager ? PeerManager.GetPeers())(1 second)
      .mapTo[List[(Long, NetworkAddress)]]
      .map(_.length)
  }

  private def getPeerInfo(): Future[List[NetworkAddress]] = {
    (peerManager ? PeerManager.GetPeers())(1 second)
      .mapTo[List[(Long, NetworkAddress)]]
      .map(peers => peers.map(_._2))
  }

  private def getBestBlockHash(): Future[Hash] = {
    Future.failed(new UnsupportedOperationException()) //TODO: implement
  }

  private def getBlockCount(): Future[Int] = {
    Future(0) //TODO: implement
  }

  private def getBlockHash(index: Int): Future[Hash] = {
    Future.failed(new UnsupportedOperationException()) //TODO: implement
  }

}