package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.pattern.pipe
import akka.actor.Actor
import akka.actor.ActorLogging

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node with APIClient with Actor with ActorLogging {
  import context.dispatcher

  def networkParams = np

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))

  override def syncWithPeer(peer: ActorRef) = {
    super.syncWithPeer(peer)
    requestBlocks(peer)
  }

  def receive: Receive =
    spvBehavior orElse nodeBehavior orElse apiClientBehavior

  def spvBehavior: Receive = {
    case Headers(hdrs) =>
      hdrs.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
    //if (!hdrs.isEmpty) {}
  }

  def getBlockLocator: Future[List[Hash]] =
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[List[Hash]]

  def requestBlocks(ref: ActorRef) =
    getBlockLocator.map(bl =>
      PeerConnection.Outgoing(
        GetHeaders(networkParams.PROTOCOL_VERSION, bl)))
      .pipeTo(ref)

  override def getChainHead(): Future[BlockChain.StoredBlock] =
    (blockchain ? BlockChain.GetChainHead())
      .mapTo[BlockChain.StoredBlock]

  override def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    (blockchain ? BlockChain.GetBlockByIndex(index))
      .mapTo[Option[BlockChain.StoredBlock]]

  override def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())

}