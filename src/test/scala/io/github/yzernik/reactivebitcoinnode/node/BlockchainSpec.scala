package io.github.yzernik.reactivebitcoinnode.node

import scala.collection.mutable.Stack

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BlockchainSpec extends FlatSpec with Matchers {

  "A Blockchain" should "start with height zero" in {
    val blockchain = new Blockchain(TestNet3Params.genesisBlock)
    blockchain.getCurrentHeight should be(0)
  }

  it should "start with the genesis block at the tip" in {
    val blockchain = new Blockchain(TestNet3Params.genesisBlock)
    blockchain.getTipBlock should be(TestNet3Params.genesisBlock)
  }

  it should "start with the correct block locator" in {
    val blockchain = new Blockchain(TestNet3Params.genesisBlock)
    val bl = List(TestNet3Params.genesisBlock.block_header.hash)
    blockchain.getBlockLocator should be(bl)
  }

}