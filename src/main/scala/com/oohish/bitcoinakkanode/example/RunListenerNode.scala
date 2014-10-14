package com.oohish.bitcoinakkanode.example

import com.oohish.bitcoinakkanode.node.ListenerNode
import com.oohish.bitcoinakkanode.wire.MainNetParams

import akka.actor.ActorSystem

object RunListenerNode extends App {

  val system = ActorSystem("node")

  val node = system.actorOf(ListenerNode.props(MainNetParams))

}