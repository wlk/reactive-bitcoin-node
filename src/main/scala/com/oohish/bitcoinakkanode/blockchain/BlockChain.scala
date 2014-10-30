package com.oohish.bitcoinakkanode.blockchain

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala

object BlockChain {

  val MAX_BLOCK_SIZE = 1000000
  val MAX_BLOCK_SIZE_GEN = MAX_BLOCK_SIZE / 2
  val MAX_BLOCK_SIGOPS = MAX_BLOCK_SIZE / 50
  val MAX_ORPHAN_TRANSACTIONS = MAX_BLOCK_SIZE / 100

  case class StoredBlock(block: Block, hash: Hash, height: Int, parent: Option[StoredBlock])
  case class GetBlockLocator()
  case class PutBlock(b: Block)
  case class GetChainHead()
  case class GetBlockByIndex(index: Int)
}

trait BlockChain extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.blockchain.BlockChain._

  def genesis: Block
  val g = StoredBlock(genesis, Util.blockHash(genesis), 0, None)
  var blocks: Map[Hash, StoredBlock] = Map(g.hash -> g)
  var chainHead = g

  def receive = {
    case GetChainHead() =>
      sender ! chainHead
    case GetBlockByIndex(index) =>
      sender ! getBlockByIndex(index)
    case GetBlockLocator() =>
      log.debug("blockchain height: {}", chainHead.height)
      sender ! blockLocator(chainHead.height)
    case PutBlock(b) =>
      receiveBlock(b)
  }

  def receiveBlock(b: Block) = {
    val prev = blocks.get(b.block_header.prev_block)
    val prevHeight = prev.map(_.height).getOrElse(0)
    val sb = StoredBlock(b, Util.blockHash(b), prevHeight + 1, prev)
    saveBlock(sb)
  }

  def saveBlock(sb: StoredBlock) = {
    blocks += sb.hash -> sb
    if (sb.height > chainHead.height) chainHead = sb
    log.debug("saved block height: {}, chainHead height: {}", sb.height, chainHead.height)
  }

  private def blockLocatorIndices(topDepth: Int): Vector[Int] = {
    // Start at max_depth
    var indices = Vector.empty[Int]
    // Push last 10 indices first
    var (step, start) = (1, 0)
    var i = topDepth
    while (i > 0) {
      if (start >= 10)
        step *= 2
      indices :+= i
      i -= step
      start += 1
    }
    indices :+= 0
    indices
  }

  def blockLocator(topDepth: Int): List[Hash] = {
    val indices = blockLocatorIndices(topDepth).toSet
    var hashes = List.empty[Hash]
    var cur: Option[StoredBlock] = Some(chainHead)
    while (cur.isDefined) {
      val sb = cur.get
      if (indices contains sb.height) {
        hashes ::= sb.hash
      }
      cur = sb.parent
    }
    hashes.reverse
  }

  private def getBlockByIndex(index: Int): Option[StoredBlock] = {
    var cur: StoredBlock = chainHead
    while (cur.height > index && cur.parent.isDefined) {
      cur = cur.parent.get
    }
    if (cur.height == index) Some(cur) else None
  }

}