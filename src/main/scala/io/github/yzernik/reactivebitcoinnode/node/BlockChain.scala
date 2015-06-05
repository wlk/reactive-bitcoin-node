package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.IO

object BlockChain {
  def props() =
    Props(classOf[BlockChain])
}

class BlockChain() extends Actor with ActorLogging {

  def receive = {
    case _ =>
  }

}