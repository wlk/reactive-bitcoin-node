package com.oohish.bitcoinakkanode.spv

import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import akka.actor.Props
import com.oohish.bitcoinakkanode.blockchain.BlockChain

object SPVBlockChain {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVBlockChain], networkParams)
}

class SPVBlockChain(networkParams: NetworkParameters) extends BlockChain {

  lazy val genesis = networkParams.genesisBlock

  def isValidBlock(b: Block) = true

}