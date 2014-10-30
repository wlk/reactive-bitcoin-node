package com.oohish.bitcoinakkanode.spv

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.math.BigInt.int2bigInt

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.blockchain.BlockChain.StoredBlock
import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.node.Node.GetBestBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetBlockCount
import com.oohish.bitcoinakkanode.node.Node.GetBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetConnectionCount
import com.oohish.bitcoinakkanode.node.Node.GetPeerInfo
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)

  val services: BigInt = 1
  val relay: Boolean = false
}

class SPVNode(val networkParams: NetworkParameters)
  extends Node with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(1 second)

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams), "spv-blockchain")
  val handler = context.actorOf(SPVHandler.props(peerManager, blockchain, networkParams), "spv-handler")

  def receive: Receive = {
    case GetConnectionCount() =>
      (peerManager ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .map(_.length)
        .pipeTo(sender)
    case GetPeerInfo() =>
      (peerManager ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .pipeTo(sender)
    case GetBestBlockHash() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[StoredBlock]
        .map(_.hash)
        .pipeTo(sender)
    case GetBlockCount() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[StoredBlock]
        .map(_.height)
        .pipeTo(sender)
    case GetBlockHash(index) =>
      (blockchain ? BlockChain.GetBlockByIndex(index))
        .mapTo[Option[StoredBlock]]
        .map(_.map(_.hash))
        .pipeTo(sender)
    case other =>
      sender ! "Command not found."
  }

}