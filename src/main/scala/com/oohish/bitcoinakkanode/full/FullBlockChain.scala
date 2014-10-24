package com.oohish.bitcoinakkanode.full

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.node.BlockChain
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Props
import scodec.bits.ByteVector

object FullBlockChain {
  def props(networkParams: NetworkParameters) =
    Props(classOf[FullBlockChain], networkParams)
}

class FullBlockChain(networkParams: NetworkParameters) extends BlockChain {

  lazy val genesis = networkParams.genesisBlock

  /*
   * https://en.bitcoin.it/wiki/Protocol_rules#.22block.22_messages
   */
  def isValidBlock(b: Block) = {
    !isDuplicateBlock(b) && // rule 2
      !hasEmptyTransactions(b) && // rule 3
      satisfiesNBits(b) && // rule 4
      !inFuture(b) && // rule 5
      validCoinbase(b) && // rule 6
      tx2to4(b) && // rule 7
      hasValidCoinBaseSigScript(b) && // rule 8
      hasTooManySigOps(b) // rule 9
  }

  def isDuplicateBlock(b: Block): Boolean = {
    val hash = blockHash(b)
    blocks.contains(hash)
  }

  def hasEmptyTransactions(b: Block): Boolean = {
    b.txs.isEmpty
  }

  def satisfiesNBits(b: Block): Boolean = {
    //b.block_header.bits
    //blockhash < target    
    true // TODO
  }

  def inFuture(b: Block): Boolean = {
    val diff = b.block_header.timestamp - (DateTime.now().getMillis() / 100)
    diff > 2 * 60 * 60
  }

  def validCoinbase(b: Block): Boolean = {
    val coinbase = b.txs(0)
    coinbase.tx_in.length == 1 &&
      coinbase.tx_in(0).previous_output.hash == Hash(ByteVector.fill(32)(0)) &&
      coinbase.tx_in(0).previous_output.index == -1
  }

  def tx2to4(b: Block): Boolean = {
    b.txs.forall(tx => true) //TODO
  }

  def hasValidCoinBaseSigScript(b: Block): Boolean = {
    val len = b.txs(0).tx_in(0).sig_script.length
    len >= 2 && len <= 100
  }

  def hasTooManySigOps(b: Block): Boolean = {
    def scriptOpCount(script: ByteVector) = 0 //TODO
    val sigOpCounts = for {
      tx <- b.txs
      txIn <- tx.tx_in
    } yield scriptOpCount(txIn.sig_script)
    sigOpCounts.sum > BlockChain.MAX_BLOCK_SIGOPS
  }

}