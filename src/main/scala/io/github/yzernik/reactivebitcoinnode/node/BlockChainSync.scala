package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ReceiveTimeout

object BlockChainSync {
  def props(blockchainController: ActorRef) =
    Props(classOf[BlockChainSync], blockchainController)

  case class SyncWithPeer(peer: ActorRef)
}

class BlockChainSync(blockchainController: ActorRef) extends Actor {
  import BlockChainSync._

  context.setReceiveTimeout(10 seconds)

  var preferredPeers = Set.empty[ActorRef]

  def receive: Receive = {
    case SyncWithPeer(p) =>
      // To set in a response to a message
      context.setReceiveTimeout(100 milliseconds)

    case ReceiveTimeout =>
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      throw new RuntimeException("Receive timed out")
  }

  def syncing(peer: ActorRef): Receive = {
    case _ =>
  }

}