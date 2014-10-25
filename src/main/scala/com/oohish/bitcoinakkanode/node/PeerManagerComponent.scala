package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.Actor

trait PeerManagerComponent extends NetworkParamsComponent {
  this: Actor =>

  val pm = context.actorOf(PeerManager.props(self, networkParams), "peer-manager")

}