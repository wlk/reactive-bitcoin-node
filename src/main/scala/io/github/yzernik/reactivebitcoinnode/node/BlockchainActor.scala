package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

object BlockchainActor {
  def props(btc: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[BlockchainActor], btc, networkParameters)
}

class BlockchainActor(btc: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {

  var blockchain = Blockchain()

  def receive = {
    case _ =>
  }

}