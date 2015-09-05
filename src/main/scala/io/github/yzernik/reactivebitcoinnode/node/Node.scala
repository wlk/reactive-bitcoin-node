package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.BTC.PeerInfo
import io.github.yzernik.reactivebitcoinnode.blockchain.BlockchainController
import io.github.yzernik.reactivebitcoinnode.blockchain.SPVBlockchainAccess
import io.github.yzernik.reactivebitcoinnode.network.PeerManager
import io.github.yzernik.reactivebitcoinnode.network.PeerManagerAccess
import io.github.yzernik.reactivebitcoinnode.network.BlockDownloader

object Node {
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
}

class Node(networkParameters: NetworkParameters, implicit val system: ActorSystem) {
  import system.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val magic = networkParameters.packetMagic
  val btc = IO(new BTC(magic, Node.services, Node.userAgent))

  val blockchainController = system.actorOf(BlockchainController.props(networkParameters, btc), name = "blockchainController")
  val blockDownloader = system.actorOf(BlockDownloader.props(blockchainController, networkParameters), name = "blockDownloader")
  val peerManager = system.actorOf(PeerManager.props(blockchainController, btc, blockDownloader, networkParameters), name = "peerManager")

  val bc = new SPVBlockchainAccess(blockchainController)
  val na = new PeerManagerAccess(peerManager)

  /**
   * Keep the peer manager periodically refreshed.
   */
  system.scheduler.schedule(0 seconds, 1 seconds, peerManager, PeerManager.UpdateConnections)

  def getBlockCount: Future[Int] =
    bc.getBlockchain.map(_.getCurrentHeight)

  def getPeerInfos: Future[List[BTC.PeerInfo]] =
    na.getPeerInfos

  def getConnectionCount: Future[Int] =
    na.getConnections.map(_.size)
}

class MainNetNode(implicit override val system: ActorSystem)
  extends Node(MainNetParams, system) 


