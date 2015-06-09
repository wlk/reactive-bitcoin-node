package io.github.yzernik.reactivebitcoinnode.node

import scala.Vector

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.BlockHeader
import io.github.yzernik.bitcoinscodec.structures.Hash

object BlockChainController {
  def props(btc: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[BlockChainController], btc, networkParameters)
}

class BlockChainController(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {

  var blockchain = BlockChain(networkParameters.genesisBlock)

  def receive = {
    case _ =>
  }

}

case class BlockChain(genesis: Block, chain: Vector[BlockHeader]) {

  def blockLocator: Vector[Hash] = ???

  def addHeader: BlockChain = ???

  def chainHead: BlockHeader = ???

  def getBlock(index: Int): BlockHeader = chain(index)

}

object BlockChain {

  def apply(genesis: Block): BlockChain = BlockChain(genesis, Vector(genesis.block_header))

}