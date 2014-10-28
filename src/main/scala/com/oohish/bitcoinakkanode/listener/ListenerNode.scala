package com.oohish.bitcoinakkanode.listener

import scala.BigInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters

import akka.actor.Actor
import akka.actor.Props

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(val networkParams: NetworkParameters)
  extends Node
  with ListenerAPIClientComponent {
  this: Actor =>
  import context.dispatcher

  override def services: BigInt = BigInt(1)
  override def relay: Boolean = false
}