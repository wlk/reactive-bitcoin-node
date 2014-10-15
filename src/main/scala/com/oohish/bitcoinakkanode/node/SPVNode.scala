package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.BlockChain.GetBlockLocatorResponse
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node {
  import context.dispatcher

  def networkParams = np
  lazy val blockchain = context.actorOf(SPVBlockChain.props(networkParams))

  def blockDownload(ref: ActorRef) = {
    log.debug("sending block locator")
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[GetBlockLocatorResponse]
      .map(blr =>
        PeerManager.UnicastMessage(
          GetHeaders(networkParams.PROTOCOL_VERSION, blr.bl), ref))
      .pipeTo(pm)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Headers(headers) =>
      log.debug("headers size: {}", headers.size)
      headers.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
      if (!headers.isEmpty) blockDownload(from)
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}