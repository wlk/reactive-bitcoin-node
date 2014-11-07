package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout.durationToTimeout

object PeerConnection {
  def props(
    manager: ActorRef,
    tcpConnection: ActorRef,
    node: ActorRef,
    remote: InetSocketAddress,
    local: InetSocketAddress,
    networkParams: NetworkParameters) =
    Props(classOf[PeerConnection], manager, tcpConnection, node, remote, local, networkParams)

  case class ConnectTimeout()
  case class Outgoing(m: Message)
  case class InitiateHandshake()
}

class PeerConnection(
  manager: ActorRef,
  tcpConnection: ActorRef,
  node: ActorRef,
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
      context.become(awaitingVersion())
      getVersion(remote, local)
        .map(TCPConnection.OutgoingMessage(_))
        .pipeTo(tcpConnection)
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
    context.become(connected(v))
    manager ! PeerManager.PeerConnected(self, remote, v)
    node ! v
  }

  def connected(v: Version): Receive = {
    case Outgoing(m) =>
      tcpConnection ! TCPConnection.OutgoingMessage(m)
    case msg: Message =>
      node ! msg
    case Terminated(ref) =>
      context.stop(self)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Future[Version] = {
    (node ? Node.GetVersion(remote, local))(1 second)
      .mapTo[Version]

  }

}