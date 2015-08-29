package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.util.Timeout
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.reactivebitcoinnode.blockchain.BlockchainModule
import io.github.yzernik.reactivebitcoinnode.network.NetworkModule
import io.github.yzernik.reactivebitcoinnode.network.PeerManager
import io.github.yzernik.reactivebitcoinnode.blockchain.BlockchainController
import io.github.yzernik.reactivebitcoinnode.network.BlockDownloader

object Node {
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
}

class Node(val networkParameters: NetworkParameters, implicit val system: ActorSystem)
    extends BlockchainModule
    with NetworkModule {
  import system.dispatcher

  implicit val timeout = Timeout(5 seconds)

  val magic = networkParameters.packetMagic
  val btc = IO(new BTC(magic, Node.services, Node.userAgent))

  val blockchainController = system.actorOf(BlockchainController.props(networkParameters, btc), name = "blockchainController")
  val blockDownloader = system.actorOf(BlockDownloader.props(blockchainController, networkParameters), name = "blockDownloader")
  val peerManager = system.actorOf(PeerManager.props(btc, blockDownloader, networkParameters), name = "peerManager")

  /**
   * Start the node on the network.
   */
  peerManager ! PeerManager.Initialize(blockchainController)

  /**
   * Keep the peer manager periodically refreshed.
   */
  system.scheduler.schedule(0 seconds, 1 seconds, peerManager, PeerManager.UpdateConnections)

}
