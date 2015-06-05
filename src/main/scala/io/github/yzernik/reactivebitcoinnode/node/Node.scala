package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.Future

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.pattern.pipe
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress
import io.github.yzernik.btcio.actors.BTC

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
  import context.system
  import Node._

  val magic = networkParameters.packetMagic
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
  val btc = IO(new BTC(magic, services, userAgent))

  val blockchain: ActorRef = context.actorOf(BlockChain.props)
  val peerManager: ActorRef = context.actorOf(PeerManager.props(btc))
  val networkController: ActorRef = context.actorOf(NetworkController.props(blockchain, peerManager, btc))

  networkController ! NetworkController.Initialize

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

  private def getConnectionCount(): Future[Int] = ???

  private def getPeerInfo(): Future[List[NetworkAddress]] = ???

  private def getBestBlockHash(): Future[Hash] = ???

  private def getBlockCount(): Future[Int] = ???

  private def getBlockHash(index: Int): Future[Hash] = ???

}
