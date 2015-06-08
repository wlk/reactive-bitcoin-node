package io.github.yzernik.reactivebitcoinnode.node

import scala.language.postfixOps

import org.joda.time.DateTime

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.btcio.actors.BTC

object PeerHandler {
  def props(listener: ActorRef, version: Version) =
    Props(classOf[PeerHandler], listener, version)

  case class Initialize(ref: ActorRef)
  case object GetInfo
}

class PeerHandler(listener: ActorRef, version: Version) extends Actor with ActorLogging {
  import PeerHandler._

  var peerInfo = PeerInfo(version)

  def receive = ready

  def ready: Receive = {
    case Initialize(ref) =>
      context.become(initialized(ref))
      ref ! BTC.Register(self)
      listener ! PeerManager.NewConnection(self)
  }

  def initialized(conn: ActorRef): Receive = {
    case BTC.Closed =>
      context.stop(self)
    case BTC.Received(msg) =>
      peerInfo = peerInfo.copy(lastTime = DateTime.now)
      listener ! PeerManager.ReceivedFromPeer(msg, sender)
    case BTC.Send(msg) =>
      log.info(s"Sending outgoing message: $msg")
      conn ! BTC.Send(msg)
    case GetInfo =>
      sender ! peerInfo
  }

}

case class PeerInfo(version: Version, startTime: DateTime, lastTime: DateTime)

object PeerInfo {

  def apply(version: Version): PeerInfo = {
    val now = DateTime.now
    PeerInfo(version, now, now)
  }

}