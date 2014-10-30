package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps
import com.oohish.bitcoinscodec.structures.Hash
import akka.actor.Actor
import com.oohish.bitcoinakkanode.wire.PeerManager
import akka.actor.ActorRef

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

trait Node extends Actor
  with NetworkParamsComponent
  with PeerManagerComponent {

  def handler: ActorRef

  override def preStart() {
    peerManager ! PeerManager.Init(handler)
  }

}