package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object NetworkController {
  def props(blockchain: ActorRef, peerManager: ActorRef, btc: ActorRef) =
    Props(classOf[NetworkController], blockchain, peerManager, btc)

  case object Initialize
}

class NetworkController(blockchain: ActorRef, peerManager: ActorRef, btc: ActorRef)
  extends Actor with ActorLogging {
  import context.system
  import NetworkController._

  var preferredDownloadPeers: Vector[ActorRef] = Vector.empty

  def receive = ready

  def ready: Receive = {
    case Initialize =>
      log.info(s"initializing network controller...")
      peerManager ! PeerManager.Initialize(self)
      context.become(syncing)
  }

  def syncing: Receive = {
    case o =>
      println(s"network controller received: $o")
  }

  def synced: Receive = {
    case _ =>
  }

}