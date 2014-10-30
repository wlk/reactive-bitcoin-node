package com.oohish.bitcoinakkanode.listener

import com.oohish.bitcoinakkanode.node.PeerMessageHandler
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Props

object ListenerHandler {
  def props(peerManager: ActorRef,
    networkParams: NetworkParameters) =
    Props(classOf[ListenerHandler], peerManager, networkParams)
}

class ListenerHandler(peerManager: ActorRef,
  val networkParams: NetworkParameters) extends PeerMessageHandler {

  override def services: BigInt = ListenerNode.services

  override def height: Int = 1

  override def relay: Boolean = ListenerNode.relay

  override def handlePeerMessage(msg: Message): Unit = msg match {
    case msg =>
      println("handled msg: {}", msg)
  }

  override def onPeerConnected(ref: ActorRef): Unit = {}

}