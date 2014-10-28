package com.oohish.bitcoinakkanode.spv

import java.net.InetSocketAddress
import scala.concurrent.Future
import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.node.APIClient
import com.oohish.bitcoinscodec.structures.Hash
import akka.actor.Props

object SPVAPIClient {
  def props() =
    Props(classOf[SPVAPIClient])
}

class SPVAPIClient() extends APIClient {

  def getConnectionCount: Future[Int] =
    Future.failed(new UnsupportedOperationException())
  def getPeerInfo: Future[List[InetSocketAddress]] =
    Future.failed(new UnsupportedOperationException())
  def getChainHead: Future[BlockChain.StoredBlock] =
    Future.failed(new UnsupportedOperationException())
  def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())
  def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())

}