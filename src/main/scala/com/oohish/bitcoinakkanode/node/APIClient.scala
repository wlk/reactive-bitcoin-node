package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.pattern.pipe

object APIClient {
  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

trait APIClient extends Actor {
  import context.dispatcher
  import com.oohish.bitcoinakkanode.node.APIClient._

  def receive: Receive = {
    case cmd: APICommand =>
      receiveAPICommand(cmd)
  }

  def receiveAPICommand(cmd: APICommand) = cmd match {
    case GetConnectionCount() =>
      getConnectionCount.pipeTo(sender)
    case GetPeerInfo() =>
      getPeerInfo.pipeTo(sender)
    case GetBestBlockHash() =>
      getChainHead.map(_.hash).pipeTo(sender)
    case GetBlockCount() =>
      getChainHead.map(_.height).pipeTo(sender)
    case GetBlockHash(index) =>
      getBlockByIndex(index).map(_.map(_.hash)).pipeTo(sender)
    case GetBlock(hash) =>
      getBlock(hash).pipeTo(sender)
  }

  def getConnectionCount: Future[Int]
  def getPeerInfo: Future[List[InetSocketAddress]]
  def getChainHead: Future[BlockChain.StoredBlock]
  def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]]
  def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]]

}