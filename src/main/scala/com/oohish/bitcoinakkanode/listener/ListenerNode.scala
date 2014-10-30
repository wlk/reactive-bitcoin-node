package com.oohish.bitcoinakkanode.listener

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters

import akka.actor.Props

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)

  val services: BigInt = 1
  val relay: Boolean = false
}

class ListenerNode(val networkParams: NetworkParameters)
  extends Node {

  val handler = context.actorOf(ListenerHandler.props(peerManager, networkParams), "listener-handler")

  def receive: Receive = {
    case _ =>
  }
}