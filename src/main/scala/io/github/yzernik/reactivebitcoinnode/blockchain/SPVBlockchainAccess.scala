package io.github.yzernik.reactivebitcoinnode.blockchain

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Block

/**
 * @author yzernik
 */
class SPVBlockchainAccess(blockchainController: ActorRef) extends BlockchainAccess {

  implicit val timeout = Timeout(5 seconds)

  def getBlockchain =
    (blockchainController ? BlockchainController.GetBlockchain).mapTo[Blockchain]

  def proposeNewBlock(block: Block) =
    blockchainController ! BlockchainController.ProposeNewBlock(block)

}