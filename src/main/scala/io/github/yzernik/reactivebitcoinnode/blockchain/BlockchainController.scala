package io.github.yzernik.reactivebitcoinnode.blockchain

import BlockchainController.GetBlockLocator
import BlockchainController.ProposeNewBlock
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.reactivebitcoinnode.node.NetworkParameters

object BlockchainController {
  def props(networkParameters: NetworkParameters, btc: ActorRef) =
    Props(classOf[BlockchainController], networkParameters, btc)

  case object GetBlockLocator
  case class ProposeNewBlock(block: Block)
  case object GetBestBlockHash
  case class GetBlock(hash: Hash)
  case object GetBlockCount
  case class GetBlockHash(index: Int)

}

class BlockchainController(networkParameters: NetworkParameters, btc: ActorRef)
    extends Actor with ActorLogging {
  import BlockchainController._

  val blockchain = new Blockchain(networkParameters.genesisBlock)

  def receive: Receive = {
    case GetBlockLocator =>
      sender ! blockchain.getBlockLocator
    case ProposeNewBlock(block) =>
      val (a, b) = blockchain.proposeNewBlock(block)
      btc ! BTC.UpdateHeight(blockchain.getCurrentHeight)
      log.info(s"Current height: ${blockchain.getCurrentHeight}, hash: ${blockchain.tip}")
  }

}