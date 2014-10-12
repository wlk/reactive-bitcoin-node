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
import scala.language.postfixOps
import scala.concurrent.duration._

object PeerConnection {
  def props(
    manager: ActorRef,
    remote: InetSocketAddress,
    local: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[PeerConnection], manager, remote, local, networkParams)

  case class ConnectTimeout()
  case class Outgoing(m: Message)
  case class Incoming(m: Message)
}

class PeerConnection(
  manager: ActorRef,
  remote: InetSocketAddress,
  local: InetSocketAddress,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import PeerConnection._
  import com.oohish.bitcoinscodec.structures.Message._
  import com.oohish.bitcoinscodec.messages._
  import context._

  override def preStart() =
    system.scheduler.scheduleOnce(5 seconds, self, ConnectTimeout())

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
    val negotiatedVersion = Math.min(networkParams.PROTOCOL_VERSION, version.version).toInt
    log.info("peer connected: {} with version {}", remote, negotiatedVersion)
    context.become(connected(negotiatedVersion))
    manager ! PeerManager.PeerConnected(self, remote)
  }

  def connected(version: Int): Receive = {
    case Outgoing(m) =>
      context.parent ! TCPConnection.OutgoingMessage(m)
    case msg: Message =>
      manager ! PeerConnection.Incoming(msg)
    case Terminated(ref) =>
      context.stop(self)
  }

}