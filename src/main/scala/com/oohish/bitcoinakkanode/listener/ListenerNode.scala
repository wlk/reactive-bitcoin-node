package com.oohish.bitcoinakkanode.listener

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.math.BigInt.int2bigInt

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.node.Node.GetConnectionCount
import com.oohish.bitcoinakkanode.node.Node.GetPeerInfo
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(val networkParams: NetworkParameters)
  extends Node {
  import context.dispatcher
  implicit val timeout = Timeout(1 second)

  override def apiBehavior: Receive = {
    case GetConnectionCount() =>
      (peerManager ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .map(_.length)
        .pipeTo(sender)
    case GetPeerInfo() =>
      (peerManager ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .pipeTo(sender)
    case other =>
      sender ! "Command not found."
  }

  override def networkBehavior: Receive = {
    case msg: Message =>
      println(s"receved message: $msg")
  }

  override def services: BigInt = 1
  override def height: Int = 1
  override def relay: Boolean = false
}