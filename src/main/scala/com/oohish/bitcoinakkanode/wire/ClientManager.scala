package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

object ClientManager {
  def props(addressManager: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[ClientManager], networkParameters)

  case class MakeOutboundConnection()
  case class ConnectToAddress(address: InetSocketAddress)
}

class ClientManager(addressManager: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import ClientManager._
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case MakeOutboundConnection() =>
      for (addr <- getRandomAddress()) connectToAddress(addr)
    case ConnectToAddress(addr) =>
      connectToAddress(addr)
  }

  /*
   * Start a connection with an address.
   */
  def connectToAddress(addr: InetSocketAddress) =
    context.actorOf(Client.props(addr, networkParameters))

  /*
   * Get a random address.
   */
  def getRandomAddress() =
    (addressManager ? AddressManager.GetAddress())
      .mapTo[java.net.InetSocketAddress]

}