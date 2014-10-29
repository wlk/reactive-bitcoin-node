package com.oohish.bitcoinakkanode.node

import akka.actor.ActorRef
import com.oohish.bitcoinakkanode.wire.PeerManager
import akka.actor.Actor

trait PeerManagerComponent {
  self: Actor with NetworkParamsComponent =>
  import context._

  val peerManager: ActorRef = context.actorOf(PeerManager.props(networkParams), "peer-manager")

}

