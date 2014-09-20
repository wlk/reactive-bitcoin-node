package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.Array.canBuildFrom
import scala.language.postfixOps
import scala.util.Try
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object PeerManager {
  def props(networkParams: NetworkParameters) =
    Props(classOf[PeerManager], networkParams)

  def seedPeers(networkParams: NetworkParameters) = for {
    fallback <- networkParams.dnsSeeds
    address <- Try(InetAddress.getAllByName(fallback))
      .getOrElse(Array())
  } yield new InetSocketAddress(address, networkParams.port)

}

class PeerManager(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import com.oohish.bitcoinscodec.structures.Message._

  def dnsPeers = PeerManager.seedPeers(networkParams)
  val pc = context.actorOf(Client.props(dnsPeers.head, networkParams))

  def receive = {
    case msg: Message =>
      log.debug("peer manager received {} from {}", msg.getClass(), sender)
      context.parent ! msg
  }

}