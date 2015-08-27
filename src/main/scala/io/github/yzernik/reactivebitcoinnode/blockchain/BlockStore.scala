package io.github.yzernik.reactivebitcoinnode.blockchain

import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.bitcoinscodec.messages.Block

trait BlockStore {

  def get(hash: Hash): Block

  def set(block: Block): Unit

}

class InMemoryBlockStore extends BlockStore {

  var m = Map.empty[Hash, Block]

  def get(hash: Hash) = m.get(hash).get

  def set(block: Block) = m += block.block_header.hash -> block

}