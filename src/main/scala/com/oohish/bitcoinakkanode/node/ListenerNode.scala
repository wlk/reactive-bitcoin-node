package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(np: NetworkParameters) extends Node {

  def networkParams = np

  lazy val blockchain = context.actorOf(ListenerBlockChain.props(networkParams))

  def blockDownload(ref: ActorRef) = {}

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}