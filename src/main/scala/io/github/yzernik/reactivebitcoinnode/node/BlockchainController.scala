package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC

object BlockchainController {
  def props(networkParameters: NetworkParameters, btc: ActorRef) =
    Props(classOf[BlockchainController], networkParameters, btc)

  trait Command
  case object GetCurrentHeight extends Command
  case class GetBlock(hash: Hash) extends Command
  case object GetTipBlock extends Command
  case class GetBlockHash(index: Int) extends Command
  case object GetBlockLocator extends Command
  case class ProposeNewBlock(block: Block) extends Command
}

class BlockchainController(networkParameters: NetworkParameters, btc: ActorRef)
  extends Actor with ActorLogging {
  import BlockchainController._

  val blockchain = new Blockchain(networkParameters.genesisBlock)

  def receive: Receive = {
    case GetCurrentHeight =>
      sender ! blockchain.getCurrentHeight
    case GetBlock(hash) =>
      sender ! blockchain.getBlock(hash)
    case GetBlockHash(index) =>
      sender ! blockchain.getHashByHeight(index)
    case GetTipBlock =>
      sender ! blockchain.getTipBlock
    case GetBlockLocator =>
      sender ! blockchain.getBlockLocator
    case ProposeNewBlock(block) =>
      val (a, b) = blockchain.proposeNewBlock(block)
      btc ! BTC.UpdateHeight(blockchain.getCurrentHeight)
    // log.info(s"Current height: ${blockchain.getCurrentHeight}, hash: ${blockchain.tip}")
  }

}