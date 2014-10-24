package com.oohish.bitcoinakkanode.spv

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.APIClient
import com.oohish.bitcoinakkanode.node.BlockChain
import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node with APIClient with Actor with ActorLogging {
  import context.dispatcher

  def networkParams = np

  implicit val timeout = Timeout(1 second)

  val pm = context.actorOf(PeerManager.props(self, networkParams))
  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))
  val downloader = context.actorOf(SPVBlockDownloader.props(self, blockchain, pm, np))

  override def syncWithPeer(peer: ActorRef, version: Version) = {
    super.syncWithPeer(peer, version)
    downloader ! SPVBlockDownloader.StartDownload(peer, version.start_height)
  }

  def receive: Receive =
    spvBehavior orElse nodeBehavior orElse apiClientBehavior

  def spvBehavior: Receive = {
    case Headers(hdrs) =>
      hdrs.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
      if (!hdrs.isEmpty) {
        val peer = sender
        getChainHead
          .map(_.height)
          .map(SPVBlockDownloader.GotBlocks(peer, _))
          .pipeTo(downloader)
      }
  }

  override def getChainHead(): Future[BlockChain.StoredBlock] =
    (blockchain ? BlockChain.GetChainHead())
      .mapTo[BlockChain.StoredBlock]

  override def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    (blockchain ? BlockChain.GetBlockByIndex(index))
      .mapTo[Option[BlockChain.StoredBlock]]

  override def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())

}