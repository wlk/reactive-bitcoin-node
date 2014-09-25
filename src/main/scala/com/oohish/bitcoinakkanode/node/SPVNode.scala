package com.oohish.bitcoinakkanode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(networkParams: NetworkParameters) extends Actor with ActorLogging {

  val pm = context.actorOf(PeerManager.props(networkParams))

  def receive = {
    case other => println(other)
  }

}