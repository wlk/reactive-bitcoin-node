package io.github.yzernik.reactivebitcoinnode.node

import scala.collection.mutable.Stack

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BlockchainSpec extends FlatSpec with Matchers {

  "A Blockchain" should "start with the null hash" in {
    val blockchain = new Blockchain
    blockchain.getCurrentHeight should be(-1)
  }

  it should "accept a genesis block" in {
    val blockchain = new Blockchain
    blockchain.proposeNewBlock(TestNet3Params.genesisBlock)
    blockchain.getCurrentHeight should be(0)
  }
}