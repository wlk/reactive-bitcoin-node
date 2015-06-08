package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.PeerInfo

object Node {
  def props(networkParameters: NetworkParameters) =
    Props(classOf[Node], networkParameters)

  sealed trait APICommand
  case object GetBestBlockHash extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case object GetBlockCount extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case object GetConnectionCount extends APICommand
  case object GetPeerInfo extends APICommand

  sealed trait APIResponse
  case class GetPeerInfoResponse(peers: Set[PeerInfo]) extends APIResponse
  case class GetConnectionCountResponse(count: Int) extends APIResponse

}

class Node(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import context.system
  import Node._

  implicit val timeout = Timeout(9 seconds)

  val magic = networkParameters.packetMagic
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
  val btc = IO(new BTC(magic, services, userAgent))

  val blockchain: ActorRef = context.actorOf(BlockChain.props)
  val peerManager: ActorRef = context.actorOf(PeerManager.props(btc, networkParameters))
  val networkController: ActorRef = context.actorOf(NetworkController.props(blockchain, peerManager, btc))

  networkController ! NetworkController.Initialize

  def receive: Receive = {
    case GetConnectionCount =>
      getConnectionCount().pipeTo(sender)
    case GetPeerInfo =>
      getPeerInfo().pipeTo(sender)
    case GetBestBlockHash =>
      getBestBlockHash.pipeTo(sender)
    case GetBlockCount =>
      getBlockCount().pipeTo(sender)
    case GetBlockHash(index) =>
      getBlockHash(index).pipeTo(sender)
    case cmd: APICommand =>
      sender ! "Command not found."
  }

  private def getConnectionCount(): Future[GetConnectionCountResponse] =
    (peerManager ? GetPeerInfo).mapTo[Set[PeerInfo]]
      .map(_.size)
      .map(GetConnectionCountResponse(_))

  private def getPeerInfo(): Future[GetPeerInfoResponse] =
    (peerManager ? GetPeerInfo).mapTo[Set[PeerInfo]]
      .map(GetPeerInfoResponse(_))

  private def getBestBlockHash(): Future[Hash] = ???

  private def getBlockCount(): Future[Int] = ???

  private def getBlockHash(index: Int): Future[Hash] = ???

}
