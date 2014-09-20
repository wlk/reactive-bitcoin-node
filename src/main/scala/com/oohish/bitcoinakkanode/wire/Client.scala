package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.BigInt
import org.joda.time.DateTime
import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp

object Client {
  def props(
    peer: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[Client], peer, networkParams)

  def version(
    recv: InetSocketAddress,
    from: InetSocketAddress,
    networkParams: NetworkParameters) = Version(
    networkParams.PROTOCOL_VERSION,
    BigInt(1),
    DateTime.now().getMillis() / 1000,
    NetworkAddress(BigInt(1), recv),
    NetworkAddress(BigInt(1), from),
    Util.genNonce,
    "/Satoshi:0.7.2/",
    1,
    true)

}

class Client(
  peer: InetSocketAddress,
  networkParams: NetworkParameters) extends Actor with ActorLogging {

  import Tcp._
  import Client._
  import com.oohish.bitcoinakkanode.wire.TCPConnection._
  import context.system

  log.debug("connecting to " + peer)
  IO(Tcp) ! Connect(peer)

  def receive = {
    case CommandFailed(_: Connect) =>
      log.debug("connect failed.")
      context stop self
    case c @ Connected(remote, local) =>
      log.debug("connected to {} from {}", remote, local)
      val connection = sender
      val handler = context.actorOf(TCPConnection.props(
        context.parent, connection, remote, local, networkParams))
      connection ! Register(handler)
      context.watch(handler)
      handler ! OutgoingMessage(version(remote, local, networkParams))
  }
}