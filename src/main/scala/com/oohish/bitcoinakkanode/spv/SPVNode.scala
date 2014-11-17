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
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinakkanode.wire.PeerConnection

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(val networkParameters: NetworkParameters)
  extends Node with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(1 second)

  val blockchain = context.actorOf(SPVBlockChain.props(networkParameters), "spv-blockchain")
  val downloader = context.actorOf(SPVBlockDownloader.props(blockchain, peerManager, networkParameters), "spv-downloader")

  override def apiBehavior: Receive = {
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

  override def networkBehavior: Receive = {
    case v: Version =>
      sender ! PeerConnection.Outgoing(GetAddr())
      downloader ! SPVBlockDownloader.StartDownload(sender, v.start_height)
    case Addr(addrs) =>
      for ((time, addr) <- addrs)
        peerManager ! PeerManager.AddAddress(addr.address)
    case Headers(hdrs) =>
      downloader ! SPVBlockDownloader.GotBlocks(sender, hdrs)
    case msg: Message =>
  }

  override def services: BigInt = 1
  override def height: Int = 1
  override def relay: Boolean = false

}