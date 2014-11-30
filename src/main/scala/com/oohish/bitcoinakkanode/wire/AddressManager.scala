package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.util.Random
import scala.util.Try

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala

object AddressManager {
  def props(networkParameters: NetworkParameters) =
    Props(classOf[AddressManager], networkParameters)

  case class GetAddress()
  case class AddAddress(addr: InetSocketAddress)
}

class AddressManager(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import AddressManager._

  var addresses = Set.empty[InetSocketAddress]

  override def preStart() = {
    for (p <- getDNSAddresses()) addresses += p
  }

  def receive = {
    case GetAddress() =>
      sender ! getAddress
    case AddAddress(addr) =>
      addAddress(addr)

  }

  /*
   * Get a random unconnected address.
   */
  def getAddress =
    Random.shuffle(addresses).headOption

  /*
   * Get a random unconnected address.
   */
  def addAddress(addr: InetSocketAddress) =
    addresses += addr

  /*
   * Get the list of addresses of DNS nodes
   */
  def getDNSAddresses(): List[InetSocketAddress] = for {
    fallback <- networkParameters.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParameters.port)

}