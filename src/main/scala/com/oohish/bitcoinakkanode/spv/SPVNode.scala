package com.oohish.bitcoinakkanode.spv

import scala.BigInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.wire.NetworkParameters

import akka.actor.Actor
import akka.actor.Props

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(val networkParams: NetworkParameters)
  extends Node
  with SPVBlockChainComponent
  with SPVAPIClientComponent {
  this: Actor =>
  import context.dispatcher

  override def services: BigInt = BigInt(1)
  override def relay: Boolean = false
}