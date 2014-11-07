package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.util.Util.currentSeconds
import com.oohish.bitcoinakkanode.util.Util.genNonce
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Node {
  val userAgent: String = "/bitcoin-akka-node:0.1.0/"

  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

trait Node extends Actor with ActorLogging {
  import Node._

  def networkParams: NetworkParameters
  def apiBehavior: Receive
  def networkBehavior: Receive
  def services: BigInt
  def height: Int
  def relay: Boolean

  val peerManager: ActorRef = context.actorOf(PeerManager.props(self, networkParams), "peer-manager")

  def receive =
    networkBehavior orElse versionBehavior orElse apiBehavior

  def versionBehavior: Receive = {
    case GetVersion(remote, local) =>
      sender ! getVersion(remote, local)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress) = Version(networkParams.PROTOCOL_VERSION,
    services,
    currentSeconds,
    NetworkAddress(services, remote),
    NetworkAddress(services, local),
    genNonce,
    Node.userAgent,
    height,
    relay)

}