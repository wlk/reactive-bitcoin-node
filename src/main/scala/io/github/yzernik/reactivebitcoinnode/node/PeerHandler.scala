package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.BaseConnection
import io.github.yzernik.btcio.actors.PeerInfo

object PeerHandler {
  def props(listener: ActorRef) =
    Props(classOf[PeerHandler], listener)

  case class Initialize(ref: ActorRef)
  case object GetInfo
}

class PeerHandler(listener: ActorRef) extends Actor with ActorLogging {
  import PeerHandler._
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

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
      listener ! PeerManager.ReceivedFromPeer(msg, sender)
    case BTC.Send(msg) =>
      log.info(s"Sending outgoing message: $msg")
      conn ! BTC.Send(msg)
    case GetInfo =>
      getPeerInfo(conn).pipeTo(sender)

  }

  private def getPeerInfo(conn: ActorRef) =
    (conn ? BaseConnection.GetPeerInfo).mapTo[PeerInfo]

}
