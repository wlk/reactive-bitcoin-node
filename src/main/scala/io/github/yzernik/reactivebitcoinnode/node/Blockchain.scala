package io.github.yzernik.reactivebitcoinnode.node

import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash

/**
 * Scala port of the Blockchain implementation at
 * https://github.com/decentraland/decentraland-core/blob/master/lib/Blockchain.js
 */
case class Blockchain(genesis: Block) {

  var tip: Hash = Hash.NULL
  var work: Map[Hash, Int] = Map(Hash.NULL -> 0)
  var height: Map[Hash, Int] = Map(Hash.NULL -> -1)
  var hashByHeight: Map[Int, Hash] = Map(-1 -> Hash.NULL)
  var next: Map[Hash, Hash] = Map.empty
  var prev: Map[Hash, Hash] = Map.empty

  val blockStore = new InMemoryBlockStore
  val txStore = new InMemoryTransactionStore

  /**
   * Initialize the blockchain with the genesis block.
   */
  this.proposeNewBlock(genesis)

  /**
   * Calculate the work.
   */
  def getWork(hash: Hash) = {
    // TODO: calculate work
    1
  }

  /**
   * Add a hash reference to the given block.
   */
  def addHashReferences(block: Block) = {
    val prevHash = block.block_header.prev_block
    val hash = block.block_header.hash
    work += hash -> (work(prevHash) + getWork(hash))
    prev += hash -> prevHash
  }

  /**
   * Save the given block in the block store.
   */
  def saveBlockToStore(block: Block) = {
    blockStore.set(block)
    //saveTxToStore(block)
  }

  /**
   * Save the given transaction in the transaction store.
   */
  def saveTxToStore(block: Block) =
    block.txs.foreach { tx =>
      txStore.set(tx)
    }

  def isValidBlock(block: Block) = {
    try {
      checkValidBlock(block)
    } catch {
      case e: BlockchainException => false
    }
    true
  }

  /**
   * Check if the given block is valid.
   */
  @throws[BlockchainException]
  private def checkValidBlock(block: Block) = {
    if (work.contains(block.block_header.prev_block)) {
      throw new BlockchainException("Missing parent")
    }
    // TODO: check transactions
    true
  }

  /**
   * Append a new block.
   */
  @throws[Exception]
  private def appendNewBlock(hash: Hash) = {
    var toUnconfirm: List[Hash] = Nil
    var toConfirm: List[Hash] = Nil
    var pointer: Hash = hash

    while (!height.contains(pointer)) {
      toConfirm ::= pointer
      pointer = prev(pointer)
    }
    var commonAncestor: Hash = pointer

    pointer = tip
    while (pointer != commonAncestor) {
      toUnconfirm ::= pointer
      pointer = prev(pointer)
    }

    //toConfirm = toConfirm.reverse
    toUnconfirm.foreach { hash =>
      unconfirm(blockStore.get(hash))
    }
    try {
      toConfirm.foreach { hash =>
        var block: Block = blockStore.get(hash)
        require(isValidBlock(block), "Block is not valid")
        confirm(block)
      }
    } catch {
      case e: BlockchainException =>
        //toUnconfirm = toUnconfirm.reverse
        toUnconfirm.foreach { hash =>
          confirm(blockStore.get(hash))
        }
        throw e
    }

    (toUnconfirm, toConfirm)
  }

  /**
   * Propose a new block for addition to the Blockchain.
   */
  @throws[Exception]
  def proposeNewBlock(block: Block): (List[Hash], List[Hash]) = {
    val prevHash = block.block_header.prev_block
    val hash = block.block_header.hash

    require(hasData(prevHash), "No previous data to estimate work")
    saveBlockToStore(block)
    addHashReferences(block)

    val blockWork = work(hash)
    val tipWork = work(this.tip)

    if (blockWork > tipWork)
      appendNewBlock(hash);
    else
      (Nil, Nil)
  }

  /**
   * Confirm a block.
   */
  def confirm(block: Block) = {
    val hash = block.block_header.hash
    val prevHash = prev(hash)
    require(prevHash != Hash.NULL || prevHash == tip,
      "Attempting to confirm a non-contiguous block.")

    tip = hash
    val blockHeight = height(prevHash) + 1
    next += prevHash -> hash
    hashByHeight += blockHeight -> hash
    height += hash -> blockHeight

    block.txs.foreach { tx =>
      // TODO: update pixels
    }
  }

  /**
   * Unconfirm a block.
   */
  def unconfirm(block: Block) = {
    val hash = block.block_header.hash
    val prevHash = prev(hash)
    require(hash == tip, "Attempting to unconfirm a non-tip block")

    tip = prevHash
    val blockHeight = height(hash)
    next -= prevHash
    hashByHeight -= blockHeight
    height -= hash

    // TODO: update pixels
  }

  /**
   * Check if the hash has data.
   */
  def hasData(hash: Hash): Boolean =
    hash == Hash.NULL || work.contains(hash)

  /**
   * Prune the Blockchain.
   */
  def prune =
    prev.keys.foreach { hash =>
      if (height.contains(hash)) {
        prev -= hash
        work -= hash
      }
    }

  /**
   * Get the block locator.
   */
  def getBlockLocator = {
    require(height.contains(tip), "The tip must have an associated height")

    var result: List[Hash] = Nil
    var currentHeight: Int = getCurrentHeight
    var exponentialBackOff: Int = 1
    for (i <- 0 to 10) {
      if (currentHeight >= 0) {
        result ::= hashByHeight(currentHeight)
        currentHeight -= 1
      }
    }

    while (currentHeight > 0) {
      result ::= hashByHeight(currentHeight)
      currentHeight -= exponentialBackOff;
      exponentialBackOff *= 2;
    }

    result.reverse
  }

  /**
   * Get the current height.
   */
  def getCurrentHeight: Int =
    height(tip)

  /**
   * Get block by a given hash.
   */
  def getBlock(hash: Hash) =
    blockStore.get(hash)

  /**
   * Get transaction by a given hash.
   */
  def getTransaction(hash: Hash) =
    txStore.get(hash)

  /**
   * Get the tip block.
   */
  def getTipBlock =
    blockStore.get(tip)

  /**
   * Get the hash by height.
   */
  def getHashByHeight(index: Int) =
    hashByHeight(index)
}

class BlockchainException(msg: String) extends RuntimeException(msg)
