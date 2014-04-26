package com.oohish.chain

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.oohish.peermessages.Block
import com.oohish.structures.char32

class MemoryBlockStore extends BlockStore {

  val blockMap: HashMap[char32, Block] = HashMap.empty[char32, Block]

  var chainHead: Option[Block] = None

  def put(block: Block): Future[Unit] =
    Future {
      blockMap.put(Chain.blockHash(block), block)
      ()
    }

  def get(hash: char32): Future[Option[Block]] =
    Future(blockMap.get(hash))

  def getChainHead(): Future[Option[Block]] =
    Future(chainHead)

  def setChainHead(cHead: Block): Future[Unit] =
    Future {
      chainHead = Some(cHead)
      ()
    }

}