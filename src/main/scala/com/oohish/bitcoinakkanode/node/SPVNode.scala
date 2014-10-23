package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.node.Node.GetBestBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetBlockCount
import com.oohish.bitcoinakkanode.node.Node.GetBlockHash
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

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node {
  import context.dispatcher

  def networkParams = np

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))

  override def onPeerConnected(peer: ActorRef) = {
    super.onPeerConnected(peer)
    requestBlocks(peer)
  }

  override def receiveMessage: PartialFunction[Message, Unit] = {
    val x: PartialFunction[Message, Unit] = {
      case Headers(hdrs) =>
        hdrs.foreach {
          blockchain ! BlockChain.PutBlock(_)
        }
      //blockDownloader ! Headers(hdrs)
    }: PartialFunction[Message, Unit]
    x orElse super.receiveMessage
  }

  override def receiveAPICommand: PartialFunction[APICommand, Unit] = {
    val x: PartialFunction[APICommand, Unit] = {
      case GetBestBlockHash() =>
        getChainHead
          .map(_.hash)
          .pipeTo(sender)
      case GetBlockCount() =>
        getChainHead
          .map(_.height)
          .pipeTo(sender)
      case GetBlockHash(index) =>
        getBlockByIndex(index)
          .map(_.map(_.hash))
          .pipeTo(sender)
    }
    x orElse super.receiveAPICommand
  }

  def requestBlocks(ref: ActorRef) = {
    log.info("requesting blocks")
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[List[Hash]]
      .map(bl =>
        PeerConnection.Outgoing(
          GetHeaders(networkParams.PROTOCOL_VERSION, bl)))
      .pipeTo(ref)
  }

  def getChainHead(): Future[BlockChain.StoredBlock] =
    (blockchain ? BlockChain.GetChainHead())
      .mapTo[BlockChain.StoredBlock]

  def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    (blockchain ? BlockChain.GetBlockByIndex(index))
      .mapTo[Option[BlockChain.StoredBlock]]

}