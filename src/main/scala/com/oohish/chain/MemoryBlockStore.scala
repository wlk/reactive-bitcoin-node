package com.oohish.chain

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MemoryBlockStore extends BlockStore {

  val blockMap: HashMap[String, StoredBlock] = HashMap.empty[String, StoredBlock]

  var chainHead: Option[StoredBlock] = None

  def put(block: StoredBlock): Future[Unit] =
    Future {
      blockMap.put(block.block.hash, block)
      ()
    }

  def get(hash: String): Future[Option[StoredBlock]] =
    Future(blockMap.get(hash))

  def getChainHead(): Future[Option[StoredBlock]] =
    Future {
      chainHead
    }

  def setChainHead(cHead: StoredBlock): Future[Unit] =
    Future {
      chainHead = Some(cHead)
    }

}