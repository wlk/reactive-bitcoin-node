package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps
import com.oohish.bitcoinscodec.structures.Hash
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.oohish.bitcoinakkanode.blockchain.BlockChain

trait BlockChainComponent {
  this: Actor =>
  import context.dispatcher

  def blockchain: ActorRef
  implicit val timeout: Timeout

  def getChainHead(): Future[BlockChain.StoredBlock] =
    (blockchain ? BlockChain.GetChainHead())
      .mapTo[BlockChain.StoredBlock]

  def getBlockByIndex(index: Int): Future[Option[BlockChain.StoredBlock]] =
    (blockchain ? BlockChain.GetBlockByIndex(index))
      .mapTo[Option[BlockChain.StoredBlock]]

  def getBlock(hash: Hash): Future[Option[BlockChain.StoredBlock]] =
    Future.failed(new UnsupportedOperationException())

}