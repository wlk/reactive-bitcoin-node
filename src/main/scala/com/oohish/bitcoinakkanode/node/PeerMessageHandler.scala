package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object PeerMessageHandler {
  case class GotMessage(msg: Message)
  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class PeerConnected(ref: ActorRef)
}

trait PeerMessageHandler extends Actor {
  import PeerMessageHandler._

  def receive: Receive = {
    case GotMessage(msg) =>
      handlePeerMessage(msg)
    case GetVersion(remote, local) =>
      sender ! getVersion(remote, local)
    case PeerConnected(ref) =>
      onPeerConnected(ref)
  }

  def handlePeerMessage(msg: Message): Unit
  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Version
  def onPeerConnected(ref: ActorRef): Unit

}