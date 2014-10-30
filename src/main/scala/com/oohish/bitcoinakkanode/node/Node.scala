package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Node {
  val userAgent: String = "/bitcoin-akka-node:0.1.0/"

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

trait Node extends Actor {

  def networkParams: NetworkParameters

  val peerManager: ActorRef = context.actorOf(PeerManager.props(networkParams), "peer-manager")
  def handler: ActorRef

  override def preStart() {
    peerManager ! PeerManager.Init(handler)
  }

}