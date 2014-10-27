package com.oohish.bitcoinakkanode.listener

import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.node.APIClient
import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(val networkParams: NetworkParameters) extends Actor with ActorLogging
  with Node
  with APIClient {

  implicit val timeout = Timeout(1 second)

  override def syncWithPeer(peer: ActorRef, version: Version) = {}
  override def services: BigInt = BigInt(1)
  override def getBlockStart(): Future[Int] = Future.successful(1)

  def receive: Receive =
    nodeBehavior orElse apiClientBehavior

  override def getChainHead: Future[BlockChain.StoredBlock] =
    Future.failed(new UnsupportedOperationException())
  override def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())
  override def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())

}