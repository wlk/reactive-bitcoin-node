package io.github.yzernik.reactivebitcoinnode.blockchain

import io.github.yzernik.bitcoinscodec.messages.Tx
import io.github.yzernik.bitcoinscodec.structures.Hash

trait TransactionStore {

  def get(hash: Hash): Tx

  def set(tx: Tx): Unit

}

class InMemoryTransactionStore extends TransactionStore {

  var m = Map.empty[Hash, Tx]

  def get(hash: Hash) = m.get(hash).get

  def set(tx: Tx) = ???

}