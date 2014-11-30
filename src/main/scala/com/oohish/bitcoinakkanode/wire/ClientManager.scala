package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

object ClientManager {
  def props(addressManager: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[ClientManager], networkParameters)

  case class Connect()
  case class ConnectToAddress(address: InetSocketAddress)
}

class ClientManager(addressManager: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import ClientManager._
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case Connect() =>
      connectToRandomAddress()
    case ConnectToAddress(addr) =>
      connectToAddress(addr)
  }

  /*
   * Start a connection with an address.
   */
  def connectToAddress(addr: InetSocketAddress) =
    context.actorOf(Client.props(addr, networkParameters))

  /*
   * Start a connection with a random address.
   */
  def connectToRandomAddress() = for {
    maybeAddr <- (addressManager ? AddressManager.GetAddress()).mapTo[Option[java.net.InetSocketAddress]]
  } maybeAddr.foreach { addr =>
    connectToAddress(addr)
  }

}