package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.BigInt
import org.joda.time.DateTime
import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress
import com.oohish.bitcoinscodec.structures.Port
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.Connect
import akka.io.Tcp.Connected
import akka.io.Tcp.Register

object Client {
  def props(peer: InetSocketAddress, networkParams: NetworkParameters) =
    Props(classOf[Client], peer, networkParams)

  DateTime.now().getMillis()

  def version(peer: InetSocketAddress, networkParams: NetworkParameters) = Version(
    networkParams.PROTOCOL_VERSION,
    BigInt(1),
    DateTime.now().getMillis() / 1000,
    NetworkAddress(BigInt(1), Util.networkAddress(peer), Port(peer.getPort())),
    NetworkAddress(BigInt(1), Util.networkAddress(
      new InetSocketAddress(InetAddress.getLocalHost(),
        networkParams.port)), Port(networkParams.port)),
    Util.genNonce,
    "/Satoshi:0.7.2/",
    1,
    None)

}

class Client(peer: InetSocketAddress, networkParams: NetworkParameters) extends Actor with ActorLogging {

  import Tcp._
  import Client._
  import com.oohish.bitcoinakkanode.wire.TCPConnection._
  import context.system

  log.info("connecting to " + peer)
  IO(Tcp) ! Connect(peer)

  def receive = {
    case CommandFailed(_: Connect) =>
      log.info("connect failed.")
      context stop self

    case c @ Connected(remote, local) =>
      log.info("connected to {} from {}", remote, local)
      val connection = sender
      val handler = context.actorOf(TCPConnection.props(context.parent, connection, networkParams.packetMagic))
      connection ! Register(handler)
      context.watch(handler)
      handler ! OutgoingMessage(version(peer, networkParams))

    case unknown => log.warning(s"$unknown")
  }
}