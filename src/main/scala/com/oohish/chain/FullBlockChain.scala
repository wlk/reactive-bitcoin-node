package com.oohish.chain

import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.Actor

object FullBlockChain {

  def props(network: String) =
    Props(classOf[FullBlockChain], network)

}

class FullBlockChain extends Actor with ActorLogging {

  def receive = {

    case _ =>
  }

}