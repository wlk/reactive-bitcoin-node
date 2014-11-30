package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

object NetworkController {
  def props(blockchain: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[NetworkController], networkParameters)
}

class NetworkController(blockchain: ActorRef,
  networkParameters: NetworkParameters) extends Actor {
  import NetworkController._

  val addressManager = AddressManager.props(networkParameters)
  val peerManager = ConnectionManager.props(networkParameters)

  def receive = {
    case _ =>
  }

}