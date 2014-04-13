package com.oohish.wire

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
  def props(peer: Peer, node: ActorRef) =
    Props(classOf[Client], peer, node)
}

class Client(peer: Peer, node: ActorRef) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  log.debug("connecting to " + peer.address.getAddress())
  IO(Tcp) ! Connect(peer.address)

  def receive = {
    case CommandFailed(_: Connect) =>
      log.debug("connect failed.........................")
      //node ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      log.debug("connected...............................")
      //node ! c
      val connection = sender
      val handler = context.actorOf(TCPConnection.props("main", peer, node, context.parent, connection))
      connection ! Register(handler)
      context.watch(handler)

    case unknown => log.warning(s"$unknown")
  }
}