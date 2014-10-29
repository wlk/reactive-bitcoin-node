package com.oohish.bitcoinakkanode.listener

import scala.BigInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters

import akka.actor.Props

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(val networkParams: NetworkParameters)
  extends Node {
  import context.dispatcher

  override def services: BigInt = BigInt(1)
  override def relay: Boolean = false

  val handler = context.actorOf(ListenerHandler.props(peerManager), "listener-handler")

  def receive: Receive = {
    case _ =>
  }
}