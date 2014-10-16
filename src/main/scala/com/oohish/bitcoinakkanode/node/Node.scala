package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import com.oohish.bitcoinscodec.structures.Hash
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Node {

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand

  sealed trait APICommandResponse
}

trait Node extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.node.Node._
  import akka.actor.actorRef2Scala
  import akka.pattern.ask
  import akka.pattern.pipe
  import akka.util.Timeout
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def networkParams: NetworkParameters

  def blockchain: ActorRef
  val pm = context.actorOf(PeerManager.props(networkParams))

  def receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      pm ! PeerManager.UnicastMessage(GetAddr(), ref)
      blockDownload(ref)
    case PeerManager.ReceivedMessage(msg, from) =>
      msgReceive(from)(msg)
    case cmd: APICommand =>
      commandReceive(cmd)
  }

  def blockDownload(ref: ActorRef): Unit

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit]

  def commandReceive: PartialFunction[APICommand, Unit] = {
    case GetBestBlockHash() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[BlockChain.StoredBlock]
        .map(_.hash)
        .pipeTo(sender)
    case GetBlockCount() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[BlockChain.StoredBlock]
        .map(_.height)
        .pipeTo(sender)
    case GetBlockHash(index) =>
      (blockchain ? BlockChain.GetBlockByIndex(index))
        .mapTo[Option[BlockChain.StoredBlock]]
        .map(_.map(_.hash))
        .pipeTo(sender)
    case GetConnectionCount() =>
      (pm ? PeerManager.GetNumConnections())
        .pipeTo(sender)
    case GetPeerInfo() =>
      (pm ? PeerManager.GetConnections())
        .pipeTo(sender)
  }

}