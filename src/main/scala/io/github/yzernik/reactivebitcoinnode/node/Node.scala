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
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.PeerInfo
import akka.actor.ActorSystem

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

}

class Node(networkParameters: NetworkParameters) extends Actor with ActorLogging
  with NetworkCommands
  with BlockchainCommands {
  import context.dispatcher
  import context.system
  import Node._

  implicit val timeout = Timeout(9 seconds)

  val magic = networkParameters.packetMagic
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
  val btc = IO(new BTC(magic, services, userAgent))

  val blockchain: ActorRef = context.actorOf(BlockChainController.props(btc, networkParameters))
  val peerManager: ActorRef = context.actorOf(PeerManager.props(btc, networkParameters))
  val networkController: ActorRef = context.actorOf(NetworkController.props(blockchain, peerManager, btc))

  networkController ! NetworkController.Initialize

  def receive: Receive = {
    case cmd: APICommand =>
      executeCommand(cmd).pipeTo(sender)
  }

  private def executeCommand(cmd: APICommand): Future[Any] = {
    cmd match {
      case GetConnectionCount  => getConnectionCount
      case GetPeerInfo         => getPeersInfo
      case GetBlockCount       => getBlockCount
      case GetBestBlockHash    => getBestBlockHash
      case GetBlockHash(index) => ???
      case GetBlock(index)     => ???
    }
  }

  private def getConnectionCount = getPeersInfo.map(_.size)

}

trait NetworkCommands { self: Node =>
  def getPeersInfo: Future[Set[PeerInfo]] =
    (peerManager ? Node.GetPeerInfo).mapTo[Set[PeerInfo]]
}

trait BlockchainCommands { self: Node =>
  def getBlockCount: Future[Int] = ???
  def getBestBlockHash: Future[Hash] = ???
  def getBlockHash(index: Int): Future[Hash] = ???
  def getBlock(hash: Hash): Future[Block] = ???
}

class NodeObj(networkParameters: NetworkParameters, implicit val _system: ActorSystem) {
  import Node._
  import _system.dispatcher

  implicit val timeout = Timeout(10 seconds)

  val node = _system.actorOf(Node.props(networkParameters))

  def getPeerInfo: Future[Set[PeerInfo]] = queryNode(GetPeerInfo).mapTo[Set[PeerInfo]]
  def getConnectionCount: Future[Int] = queryNode(GetConnectionCount).mapTo[Int]

  private def queryNode(cmd: Node.APICommand) = node ? cmd

}
