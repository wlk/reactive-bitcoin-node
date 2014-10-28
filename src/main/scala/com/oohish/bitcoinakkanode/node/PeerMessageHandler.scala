package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor

trait PeerMessageHandler extends Actor {

  def receive: Receive = {
    case msg: Message =>
      handlePeerMessage(msg)
  }

  def handlePeerMessage(msg: Message): Unit

}