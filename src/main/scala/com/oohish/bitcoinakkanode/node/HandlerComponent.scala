package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

trait HandlerComponent {
  self: Actor with PeerManagerComponent =>

  def handler: ActorRef
  peerManager ! PeerManager.Init(handler)

}

