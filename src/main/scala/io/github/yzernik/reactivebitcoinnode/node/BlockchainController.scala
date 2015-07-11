package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.btcio.actors.BTC

object BlockchainController {
  def props(networkParameters: NetworkParameters, btc: ActorRef) =
    Props(classOf[BlockchainController], networkParameters, btc)

  case object GetBlockLocator
  case class ProposeNewBlock(block: Block)

}

class BlockchainController(networkParameters: NetworkParameters, btc: ActorRef)
  extends Actor with ActorLogging {
  import BlockchainController._

  val blockchain = new Blockchain(networkParameters.genesisBlock)

  def receive: Receive = {
    case cmd: Node.BlockchainCommand =>
      handleNetworkAPICommand(cmd)
    case GetBlockLocator =>
      sender ! blockchain.getBlockLocator
    case ProposeNewBlock(block) =>
      val (a, b) = blockchain.proposeNewBlock(block)
      btc ! BTC.UpdateHeight(blockchain.getCurrentHeight)
      log.info(s"Current height: ${blockchain.getCurrentHeight}, hash: ${blockchain.tip}")
  }

  /**
   * Handle a network API command.
   */
  private def handleNetworkAPICommand(cmd: Node.BlockchainCommand) = {
    cmd match {
      case Node.GetBestBlockHash =>
        sender ! blockchain.getTipBlock.block_header.hash
      case Node.GetBlock(hash) =>
        sender ! blockchain.getBlock(hash)
      case Node.GetBlockCount =>
        sender ! blockchain.getCurrentHeight
      case Node.GetBlockHash(index) =>
        sender ! blockchain.getHashByHeight(index)
    }
  }

}