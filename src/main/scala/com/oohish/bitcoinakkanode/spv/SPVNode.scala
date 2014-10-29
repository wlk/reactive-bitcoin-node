package com.oohish.bitcoinakkanode.spv

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.math.BigInt.int2bigInt

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.node.Node.GetConnectionCount
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(val networkParams: NetworkParameters)
  extends Node with ActorLogging {
  import context.dispatcher

  override def services: BigInt = 1
  override def relay: Boolean = false

  val handler = context.actorOf(SPVHandler.props(peerManager, networkParams), "spv-handler")

  implicit val timeout = Timeout(1 second)

  def receive: Receive = {
    case GetConnectionCount() =>
      (peerManager ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .map(_.length)
        .pipeTo(sender)
    case other =>
      log.info("got: {}", other)
  }

}