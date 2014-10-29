package com.oohish.bitcoinakkanode.listener

import java.net.InetSocketAddress

import scala.BigInt

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.node.PeerMessageHandler
import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.ActorRef
import akka.actor.Props

object ListenerHandler {
  def props(peerManager: ActorRef) =
    Props(classOf[ListenerHandler], peerManager)
}

class ListenerHandler(peerManager: ActorRef) extends PeerMessageHandler {

  def handlePeerMessage(msg: Message): Unit = msg match {
    case msg =>
      println("handled msg: {}", msg)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Version =
    Version(
      7000,
      BigInt(1),
      DateTime.now().getMillis() / 1000,
      NetworkAddress(BigInt(1), remote),
      NetworkAddress(BigInt(1), local),
      Util.genNonce,
      "/Satoshi:0.7.2/",
      1,
      true)

  def onPeerConnected(ref: ActorRef): Unit = {}

}