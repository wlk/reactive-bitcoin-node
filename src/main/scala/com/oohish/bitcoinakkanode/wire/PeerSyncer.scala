package com.oohish.bitcoinakkanode.wire

import com.oohish.bitcoinscodec.messages.GetAddr

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object PeerSyncer {
  def props(blockchain: ActorRef, addressManager: ActorRef, peerManager: ActorRef) =
    Props(classOf[PeerSyncer], blockchain, addressManager, peerManager)

  case class SyncWithPeer(peerConnection: ActorRef)
}

class PeerSyncer(blockchain: ActorRef, addressManager: ActorRef, peerManager: ActorRef)
  extends Actor with ActorLogging {
  import PeerSyncer._

  def receive = {
    case SyncWithPeer(pc) =>
      pc ! PeerConnection.OutgoingMessage(GetAddr())
    // TODO: get block headers
  }

}