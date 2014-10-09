package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.Array.canBuildFrom
import scala.math.BigInt.int2bigInt
import scala.util.Random
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import com.oohish.bitcoinscodec.structures.Message.Message

object BTCConnection {
  def props(
    manager: ActorRef,
    remote: InetSocketAddress,
    local: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[BTCConnection], manager, remote, local, networkParams)

  case class ConnectTimeout()
  case class Outgoing(m: Message)
}

class BTCConnection(
  manager: ActorRef,
  remote: InetSocketAddress,
  local: InetSocketAddress,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import BTCConnection._
  import com.oohish.bitcoinscodec.structures.Message._
  import com.oohish.bitcoinscodec.messages._

  def receive = connecting(false, None)

  def connecting(verackReceived: Boolean, versionReceived: Option[Version]): Receive = {
    case v: Version =>
      log.info("btc connection received version")
      context.parent ! Verack()
      if (verackReceived) {
        finishHandshake(v, v.timestamp)
      } else {
        context.parent ! Client.version(remote, local, networkParams)
        context.become(connecting(false, Some(v)))
      }
    case _: Verack =>
      log.info("btc connection received verack")
      if (versionReceived.isDefined) {
        val v = versionReceived.get
        finishHandshake(v, v.timestamp)
      } else {
        context.become(connecting(true, None))
      }
    case m: ConnectTimeout =>
      log.info("btc connection received connect timeout")
      context.stop(self)
  }

  def finishHandshake(version: Version, time: Long): Unit = {
    //manager ! PeerConnected(peer, time)
    // take the minimum of the client version and the connected peer's version.
    val negotiatedVersion = Math.min(networkParams.PROTOCOL_VERSION, version.version).toInt
    log.info("peer connected: {} with version {}", remote, negotiatedVersion)
    context.become(connected(negotiatedVersion))
    manager ! PeerManager.PeerConnected(self)
  }

  def connected(version: Int): Receive = {
    case Outgoing(m) =>
      log.info("btc connection sending message: " + m)
      context.parent ! TCPConnection.OutgoingMessage(m)
    case m: Message =>
      log.debug("received message: {}", m)
      manager ! m
    case Terminated(ref) =>
      context.stop(self)
  }

}