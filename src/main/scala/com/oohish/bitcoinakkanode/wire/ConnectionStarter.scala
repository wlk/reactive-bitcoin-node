package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object ConnectionStarter {
  def props(peerManager: ActorRef, addressManager: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[ConnectionStarter], peerManager, addressManager, networkParameters)

  case class MakeConnection()
}

class ConnectionStarter(peerManager: ActorRef, addressManager: ActorRef,
  networkParameters: NetworkParameters)
  extends Actor with ActorLogging {
  import ConnectionStarter._

  val clientManager = context.actorOf(ClientManager.props(peerManager, addressManager, networkParameters))
  //val serverManager = context.actorOf(ServerManager.props(addressManager, networkParameters))

  def receive = {
    case MakeConnection() =>
      log.info("make new connections.")
      //serverManager ! ServerManager.AcceptConnections()
      clientManager ! ClientManager.MakeOutboundConnection()
  }

}