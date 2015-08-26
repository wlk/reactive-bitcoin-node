package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC

object Node {
  def props(networkParameters: NetworkParameters) =
    Props(classOf[Node], networkParameters)

  sealed trait APICommand

  // categories of API commands
  sealed trait BlockchainCommand extends APICommand
  sealed trait NetworkCommand extends APICommand

  // blockchain commands
  case object GetBestBlockHash extends BlockchainCommand
  case class GetBlock(hash: Hash) extends BlockchainCommand
  case object GetBlockCount extends BlockchainCommand
  case class GetBlockHash(index: Int) extends BlockchainCommand

  // network commands
  case object GetConnectionCount extends NetworkCommand
  case object GetPeerInfo extends NetworkCommand

}

class Node(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import context.system
  import Node._

  implicit val timeout = Timeout(10 seconds)

  val magic = networkParameters.packetMagic
  val services = BigInt(1L)
  val userAgent = "reactive-btc"
  val btc = IO(new BTC(magic, services, userAgent))

  val blockchainController = context.actorOf(BlockchainController.props(networkParameters, btc), name = "blockchainController")
  val blockDownloader = context.actorOf(BlockDownloader.props(blockchainController, networkParameters), name = "blockDownloader")
  val peerManager = context.actorOf(PeerManager.props(btc, blockDownloader, networkParameters), name = "peerManager")

  /**
   * Start the node on the network.
   */
  peerManager ! PeerManager.Initialize(blockchainController)

  /**
   * Keep the peer manager periodically refreshed.
   */
  context.system.scheduler.schedule(0 seconds, 1 seconds, peerManager, PeerManager.UpdateConnections)

  def receive: Receive = {
    case cmd: BlockchainCommand => blockchainController forward cmd
    case cmd: NetworkCommand    => peerManager forward cmd
  }

}
