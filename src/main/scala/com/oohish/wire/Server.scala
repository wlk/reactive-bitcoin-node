package com.oohish.wire

import akka.actor.Actor
import akka.io.Tcp.Bound
import akka.io.Tcp.CommandFailed
import akka.io.IO
import akka.io.Tcp
import akka.io.Tcp.Connected
import akka.actor.Props
import java.net.InetSocketAddress

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 8333))

  def receive = {
    case b @ Bound(localAddress) =>
    // do some logging or setup ...

    case CommandFailed(_: Bind) => context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender
      connection ! Register(handler)
  }

}