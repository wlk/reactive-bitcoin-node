package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import akka.actor.Props

object ListenerBlockChain {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerBlockChain], networkParams)
}

class ListenerBlockChain(networkParams: NetworkParameters) extends BlockChain {

  lazy val genesis = networkParams.genesisBlock

  def isValidBlock(b: Block) = false

}