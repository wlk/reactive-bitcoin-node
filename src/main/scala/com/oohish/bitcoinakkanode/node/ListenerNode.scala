package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps
import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.oohish.bitcoinakkanode.wire.PeerConnection

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(np: NetworkParameters) extends Node {

  def networkParams = np

}