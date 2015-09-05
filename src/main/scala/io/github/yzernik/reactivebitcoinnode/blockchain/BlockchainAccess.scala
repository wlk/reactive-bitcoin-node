package io.github.yzernik.reactivebitcoinnode.blockchain

import scala.concurrent.Future

import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash

/**
 * @author yzernik
 */
trait BlockchainAccess {

  def getBlockchain: Future[Blockchain]

  def proposeNewBlock(block: Block): Unit

  def getBlockLocator(implicit executor: scala.concurrent.ExecutionContext) =
    getBlockchain.map { _.getBlockLocator }

}