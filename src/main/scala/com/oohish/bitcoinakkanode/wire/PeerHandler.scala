package com.oohish.bitcoinakkanode.wire

import akka.actor.Props
import java.net.InetSocketAddress
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef

object PeerHandler {
  def props(tcpConn: ActorRef, remote: InetSocketAddress,
    local: InetSocketAddress, networkParameters: NetworkParameters) =
    Props(classOf[PeerHandler], tcpConn, remote, local, networkParameters)
}

class PeerHandler(tcpConn: ActorRef, remote: InetSocketAddress,
  local: InetSocketAddress, networkParameters: NetworkParameters) extends Actor with ActorLogging {

  def receive = {
    case _ =>
  }

}