package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.BlockChain.GetBlockLocatorResponse
import com.oohish.bitcoinakkanode.wire._
import com.oohish.bitcoinscodec.messages.GetHeaders

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))
  val pm = context.actorOf(PeerManager.props(networkParams))

  def receive = {
    case PeerManager.PeerConnected(ref) =>
      log.info("spv node notified peer connected")
      (blockchain ? BlockChain.GetBlockLocator())
        .mapTo[GetBlockLocatorResponse]
        .map(blr => BTCConnection.Outgoing(GetHeaders(networkParams.PROTOCOL_VERSION, blr.bl)))
        .pipeTo(ref)
    case BlockChain.GetBlockLocatorResponse(bl) =>
      pm ! GetHeaders(1, bl)
    case other => println(other + "-------------------------------------------------------")
  }

}