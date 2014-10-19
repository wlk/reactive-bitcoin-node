package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout
import akka.actor.Cancellable

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(np: NetworkParameters) extends Node {

  def networkParams = np

  lazy val blockchain = context.actorOf(ListenerBlockChain.props(networkParams))

  def requestBlocks(ref: ActorRef) = {}

  def syncing(conn: ActorRef, timeout: Cancellable): Receive = ready

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}