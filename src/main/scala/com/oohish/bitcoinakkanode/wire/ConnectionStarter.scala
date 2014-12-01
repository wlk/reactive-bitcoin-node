package com.oohish.bitcoinakkanode.wire

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

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
  import context._

  val clientManager = context.actorOf(ClientManager.props(peerManager, addressManager, networkParameters))
  //val serverManager = context.actorOf(ServerManager.props(addressManager, networkParameters))

  system.scheduler.schedule(0 seconds, 1 second, self, MakeConnection())

  def receive = {
    case MakeConnection() =>
      //serverManager ! ServerManager.AcceptConnections()
      clientManager ! ClientManager.MakeOutboundConnection()
  }

}