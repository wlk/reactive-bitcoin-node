package io.github.yzernik.reactivebitcoinnode.node

import scala.language.postfixOps
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.bitcoinscodec.messages.GetAddr

object PeerHandler {
  def props(blockchainController: ActorRef, peerManager: ActorRef) =
    Props(classOf[PeerHandler], blockchainController, peerManager)

  case class Initialize(conn: ActorRef, inbound: Boolean)

}

class PeerHandler(blockchainController: ActorRef, peerManager: ActorRef) extends Actor with ActorLogging {
  import PeerHandler._

  def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case Initialize(conn, inbound) =>
      context.watch(conn)
      context.become(ready(conn))
      conn ! BTC.Register(self)
      initialSync(conn, inbound)
  }

  def ready(conn: ActorRef): Receive = {
    case BTC.Closed =>
      context.stop(self)
    case Terminated(ref) =>
      context.stop(self)
    case other =>
      log.info(s"Peer Handler received message: $other")
  }

  private def initialSync(conn: ActorRef, inbound: Boolean) = {
    log.info(s"Doing initial sync with peer: $conn")
    // TODO: initial peer sync
    conn ! BTC.Send(GetAddr())
  }

}