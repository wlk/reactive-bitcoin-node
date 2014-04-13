package com.oohish.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.io.Tcp.PeerClosed
import akka.io.Tcp.Received
import com.oohish.wire.Node.Incoming

object SimplisticHandler {
  def props() =
    Props(classOf[SimplisticHandler])
}

class SimplisticHandler extends Actor with ActorLogging {
  import akka.io.Tcp._
  def receive = {
    case Incoming(peer, msg) => {
      log.info("SimplisticHandler received data:" + msg + " from: " + peer)
      //sender ! Write(data)
    }
    case other => {
      log.warning("got other: " + other)
    }
  }
}