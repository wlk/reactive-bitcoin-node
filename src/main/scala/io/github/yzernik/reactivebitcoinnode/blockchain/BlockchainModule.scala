package io.github.yzernik.reactivebitcoinnode.blockchain

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.reactivebitcoinnode.node.NetworkParameters

/**
 * @author yzernik
 */
trait BlockchainModule {

  implicit val timeout: Timeout

  def blockchainController: ActorRef

  def getBestBlockHash =
    (blockchainController ? BlockchainController.GetBestBlockHash).mapTo[Hash]

  def getBlock(hash: Hash) =
    (blockchainController ? BlockchainController.GetBlock(hash)).mapTo[Block]

  def getBlockCount =
    (blockchainController ? BlockchainController.GetBlockCount).mapTo[Int]

  def getBlockHash(index: Int) =
    (blockchainController ? BlockchainController.GetBlockHash(index)).mapTo[Hash]

  def getBlockLocator =
    (blockchainController ? BlockchainController.GetBlockLocator).mapTo[List[Hash]]

  def proposeNewBlock(block: Block) =
    blockchainController ! BlockchainController.ProposeNewBlock(block)

}