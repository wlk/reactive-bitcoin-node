package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.BigInt
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala

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
  case class InitiateHandshake()
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

  private var timeoutReminder: Cancellable = _

  override def preStart() =
    timeoutReminder = system.scheduler.scheduleOnce(5 seconds, self, ConnectTimeout())

  override def postStop(): Unit = timeoutReminder.cancel()

  def receive = ready()

  def ready(): Receive = {
    case InitiateHandshake() =>
      context.parent ! TCPConnection.OutgoingMessage(version())
      context.become(awaitingVersion())
    case v: Version =>
  }

  def awaitingVersion(): Receive = {
    case v: Version =>
      context.become(awaitingVerack(v))
    case _: ConnectTimeout =>
      context.stop(self)
  }

  def awaitingVerack(v: Version): Receive = {
    case _: Verack =>
      finishHandshake(v)
    case _: ConnectTimeout =>
      context.stop(self)
  }

  def finishHandshake(v: Version): Unit = {
    val verNum = Math.min(networkParams.PROTOCOL_VERSION, v.version).toInt
    context.become(connected(verNum))
    manager ! PeerManager.PeerConnected(self, remote)
  }

  def connected(verNum: Int): Receive = {
    case Outgoing(m) =>
      context.parent ! TCPConnection.OutgoingMessage(m)
    case msg: Message =>
      manager ! PeerConnection.Incoming(msg)
    case Terminated(ref) =>
      context.stop(self)
  }

  private def version() = Version(
    networkParams.PROTOCOL_VERSION,
    BigInt(1),
    DateTime.now().getMillis() / 1000,
    NetworkAddress(BigInt(1), remote),
    NetworkAddress(BigInt(1), local),
    Util.genNonce,
    "/Satoshi:0.7.2/",
    1,
    true)

}