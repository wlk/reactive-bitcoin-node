package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

object NetworkController {
  def props(blockchain: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[NetworkController], blockchain, networkParameters)

  case class ConnectToAddress(address: InetSocketAddress)
  case class AddAddress(address: InetSocketAddress)
  case class GetPeers()
}

class NetworkController(blockchain: ActorRef,
  networkParameters: NetworkParameters) extends Actor {
  import NetworkController._

  val addressManager = context.actorOf(AddressManager.props(networkParameters), "address-manager")
  val peerManager = context.actorOf(PeerManager.props(blockchain, addressManager, networkParameters), "peer-manager")
  val connectionStarter = context.actorOf(ConnectionStarter.props(peerManager,
    addressManager, networkParameters), "connection-starter")

  def receive = {
    case GetPeers() =>
    case ConnectToAddress(addr) =>
    case AddAddress(addr) =>
  }

}