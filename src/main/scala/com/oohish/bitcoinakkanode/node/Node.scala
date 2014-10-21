package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object Node {

  case class SyncTimeout()

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand

  sealed trait APICommandResponse
}

trait Node extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.node.Node._
  import akka.actor.actorRef2Scala
  import akka.pattern.ask
  import akka.pattern.pipe
  import akka.util.Timeout
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def networkParams: NetworkParameters

  val pm = context.actorOf(PeerManager.props(networkParams))

  def receiveNetworkCommand: PartialFunction[APICommand, Unit] = {
    case GetConnectionCount() =>
      (pm ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .map(_.length)
        .pipeTo(sender)
    case GetPeerInfo() =>
      (pm ? PeerManager.GetPeers())
        .mapTo[List[InetSocketAddress]]
        .pipeTo(sender)
  }

}