package com.oohish.bitcoinakkanode.example

import com.oohish.bitcoinakkanode.node.SPVNode
import com.oohish.bitcoinakkanode.wire.MainNetParams

import akka.actor.ActorSystem

object RunSPVNode extends App {

  val system = ActorSystem("node")

  // start the peer manager
  val pm = system.actorOf(SPVNode.props(MainNetParams))

}