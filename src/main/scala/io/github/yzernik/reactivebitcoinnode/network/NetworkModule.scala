package io.github.yzernik.reactivebitcoinnode.network

import scala.language.postfixOps

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.reactivebitcoinnode.node.NetworkParameters

/**
 * @author yzernik
 */
trait NetworkModule {

  def system: ActorSystem

  def networkParameters: NetworkParameters

  def btc: ActorRef

  def blockchainController: ActorRef

  implicit val timeout: Timeout

  lazy val blockDownloader = system.actorOf(BlockDownloader.props(blockchainController, networkParameters), name = "blockDownloader")

  lazy val peerManager = system.actorOf(PeerManager.props(btc, blockDownloader, networkParameters), name = "peerManager")

  def getConnectionCount =
    (peerManager ? PeerManager.GetConnectionCount).mapTo[Int]

}