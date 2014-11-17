package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.Actor
import akka.actor.ActorRef

trait PeerManagerComponent {
  this: Actor with NetworkParametersComponent =>

  val peerManager: ActorRef = context.actorOf(PeerManager.props(self, networkParameters), "peer-manager")

}