package com.oohish.bitcoinakkanode.spv

import com.oohish.bitcoinakkanode.node.PeerMessageHandler
import com.oohish.bitcoinscodec.messages.Ping
import com.oohish.bitcoinscodec.messages.Pong
import com.oohish.bitcoinscodec.structures.Message
import akka.actor.actorRef2Scala
import akka.actor.ActorRef

class SPVPeerMessageHandler(peerManager: ActorRef, blockChain: ActorRef) extends PeerMessageHandler {

  def handlePeerMessage(msg: Message): Unit = msg match {
    case Ping(nonce) =>
      sender ! Pong(nonce)
  }

}