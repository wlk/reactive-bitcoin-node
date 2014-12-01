package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress
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
import akka.actor.ActorRef

object Client {
  def props(peerManager: ActorRef, address: InetSocketAddress,
    networkParameters: NetworkParameters) =
    Props(classOf[Client], peerManager, address, networkParameters)
}

class Client(peerManager: ActorRef, address: InetSocketAddress,
  networkParameters: NetworkParameters)
  extends Actor with ActorLogging {
  import Tcp._
  import Client._
  import context.system

  log.debug("connecting to " + address)
  IO(Tcp) ! Connect(address)

  def receive = {
    case CommandFailed(_: Connect) =>
      log.debug("connect failed.")
      context stop self
    case c @ Connected(remote, local) =>
      val tcpConn = sender
      val handshaker = context.actorOf(Handshaker.props(tcpConn, remote, local, networkParameters), "handshaker")
      tcpConn ! Register(handshaker)
      context.watch(handshaker)
      handshaker ! Handshaker.InitiateHandshake()
    case Handshaker.FinishedHandshake(tcpConn) =>
      peerManager ! PeerManager.PeerConnected(tcpConn, address, null)
    case _: akka.actor.Terminated =>
      context.stop(self)
  }
}