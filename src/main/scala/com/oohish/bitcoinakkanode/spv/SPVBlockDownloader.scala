package com.oohish.bitcoinakkanode.spv

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout

object SPVBlockDownloader {
  def props(node: ActorRef,
    blockchain: ActorRef,
    pm: ActorRef,
    np: NetworkParameters) =
    Props(classOf[SPVBlockDownloader], node, blockchain, pm, np)

  case class StartDownload(peer: ActorRef, blockCount: Int)
  case class GotBlocks(peer: ActorRef, blockCount: Int)
  case class DownloadTimeout()
}

class SPVBlockDownloader(node: ActorRef,
  blockchain: ActorRef,
  pm: ActorRef,
  np: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import SPVBlockDownloader._

  implicit val timeout = Timeout(1 second)

  def receive: Receive = ready

  def ready: Receive = {
    case StartDownload(peer, blockCount) =>
      downloadFromPeer(peer, blockCount)
  }

  def downloading(peer: ActorRef, blockCount: Int, timeout: Cancellable): Receive = {
    case GotBlocks(p, count) if p == peer =>
      timeout.cancel
      if (count < blockCount) downloadFromPeer(peer, blockCount)
      else context.become(ready)
    case DownloadTimeout() =>
      getRandomPeer.onSuccess {
        case Some(conn) =>
          downloadFromPeer(conn, blockCount)
      }
  }

  def downloadFromPeer(peer: ActorRef, blockCount: Int) = {
    val t = context.system.scheduler.
      scheduleOnce(5.second, self, DownloadTimeout())
    context.become(downloading(peer, blockCount, t))
    requestBlocks(peer)
  }

  def getBlockLocator: Future[List[Hash]] =
    (blockchain ? BlockChain.GetBlockLocator())(1 second)
      .mapTo[List[Hash]]

  def requestBlocks(ref: ActorRef) = {
    getBlockLocator.map(bl =>
      PeerConnection.Outgoing(
        GetHeaders(np.PROTOCOL_VERSION, bl)))
      .pipeTo(ref)
  }

  def getRandomPeer =
    (pm ? PeerManager.GetRandomConnection())
      .mapTo[Option[ActorRef]]
}