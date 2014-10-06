package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash

object BlockChain {
  def props(networkParams: NetworkParameters) =
    Props(classOf[BlockChain], networkParams)

  case class StoredBlock(b: Block, height: Long)
}

class BlockChain(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.node.BlockChain._

  val genesis = StoredBlock(networkParams.genesisBlock, 1)
  var blocks: Map[Hash, StoredBlock] = Map.empty
  var chainHead = genesis

  def receive = {
    case _ =>
  }

}