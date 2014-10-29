package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.Connect
import akka.io.Tcp.Connected
import akka.io.Tcp.Register

object Client {
  def props(
    handler: ActorRef,
    peer: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[Client], handler, peer, networkParams)
}

class Client(
  handler: ActorRef,
  peer: InetSocketAddress,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import Tcp._
  import Client._
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
      val tcpConn = context.actorOf(TCPConnection.props(
        context.parent, handler, connection, remote, local, networkParams, true), "tcp-connection")
      connection ! Register(tcpConn)
      context.watch(tcpConn)
    case _: akka.actor.Terminated =>
      context.stop(self)
  }
}