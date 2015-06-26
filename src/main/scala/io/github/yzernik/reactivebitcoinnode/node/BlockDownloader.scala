package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

object BlockDownloader {
  def props(blockchainController: ActorRef) =
    Props(classOf[BlockDownloader], blockchainController)

  case class RequestDownload(conn: ActorRef)
}

class BlockDownloader(blockchainController: ActorRef) extends Actor with ActorLogging {
  import BlockDownloader._

  var downloadPeers = Set.empty[ActorRef]

  def receive = {
    case _ =>
  }

  def idle: Receive = {
    case RequestDownload(conn) =>
      val p = sender
      downloadPeers += p
      //p ! ContinueDownload()
      //p ! BTC.
      context.become(downloading(p))
  }

  def downloading(peer: ActorRef): Receive = {
    case _ =>
  }

}