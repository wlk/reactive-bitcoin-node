package com.oohish.chain

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.oohish.peermessages.Block
import com.oohish.structures.char32

class MemoryBlockStore extends BlockStore {

  val blockMap: HashMap[char32, StoredBlock] = HashMap.empty[char32, StoredBlock]

  var chainHead: Option[StoredBlock] = None

  def put(block: StoredBlock): Future[Unit] =
    Future {
      blockMap.put(block.block.hash, block)
      ()
    }

  def get(hash: char32): Future[Option[StoredBlock]] =
    Future(blockMap.get(hash))

  def getChainHead(): Option[StoredBlock] =
    chainHead

  def setChainHead(cHead: StoredBlock): Unit =
    chainHead = Some(cHead)

}