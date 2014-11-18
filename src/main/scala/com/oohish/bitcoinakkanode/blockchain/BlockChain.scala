package com.oohish.bitcoinakkanode.blockchain

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import com.oohish.bitcoinscodec.structures.BlockHeader

object BlockChain {

  case class SavedHeader(header: BlockHeader, hash: Hash, height: Int)

  sealed trait BlockChainCommand
  case class GetBlockLocator() extends BlockChainCommand
  case class PutHeader(bh: BlockHeader) extends BlockChainCommand
  case class PutBlock(b: Block) extends BlockChainCommand
  case class GetChainHead() extends BlockChainCommand
  case class GetBlockByIndex(index: Int) extends BlockChainCommand
}

trait BlockChain extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.blockchain.BlockChain._

  def genesis: BlockHeader

  val root = SavedHeader(genesis, Util.blockHash(genesis), 0)
  var savedHeaders: Map[Hash, SavedHeader] = Map(Util.blockHash(genesis) -> root)
  var chainHead = root

  def receive = {
    case GetChainHead() =>
      sender ! chainHead
    case GetBlockByIndex(index) =>
      sender ! getBlockByIndex(index)
    case GetBlockLocator() =>
      log.debug("blockchain height: {}", chainHead.height)
      sender ! blockLocator(chainHead.height)
    case PutBlock(b) =>
      println(b)
      saveHeader(b.block_header)
    case PutHeader(bh) =>
      saveHeader(bh)
  }

  def saveHeader(header: BlockHeader) = {
    val maybePrev = savedHeaders.get(header.prev_block)
    maybePrev.foreach { savedHdr =>
      val newSavedHdr = SavedHeader(header, Util.blockHash(header), savedHdr.height + 1)
      val hash = Util.blockHash(header)
      savedHeaders += hash -> newSavedHdr

      if (newSavedHdr.height > chainHead.height) {
        chainHead = newSavedHdr
      }
      log.debug("saved block height: {}, chainHead height: {}", newSavedHdr.height, chainHead.height)
    }
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
    var cur: Option[SavedHeader] = Some(chainHead)
    while (cur.isDefined) {
      val sb = cur.get
      if (indices contains sb.height) {
        hashes ::= sb.hash
      }
      cur = savedHeaders.get(sb.header.prev_block)
    }
    hashes.reverse
  }

  private def getBlockByIndex(index: Int): Option[SavedHeader] = {
    var cur: SavedHeader = chainHead
    while (cur.height > index && cur.height >= 0) {
      cur = savedHeaders.get(cur.header.prev_block).get
    }
    if (cur.height == index) Some(cur) else None
  }

  /*  private def getHashedBlockHeader(header: BlockHeader): HashedHeader = {
    HashedHeader(header, Util.blockHash(header))
  }*/

}