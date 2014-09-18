package com.oohish.bitcoinakkanode.example

import akka.actor.ActorSystem
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinakkanode.wire.MainNetParams

object ListenerNode extends App {

  val system = ActorSystem("node")

  // start the peer manager
  val pm = system.actorOf(PeerManager.props(MainNetParams))

}