package com.oohish.bitcoinakkanode.spv

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.blockchain.BlockChain.StoredBlock
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout

object SPVBlockDownloader {
  def props(blockchain: ActorRef,
    peerManager: ActorRef,
    networkParams: NetworkParameters) =
    Props(classOf[SPVBlockDownloader], blockchain, peerManager, networkParams)

  case class StartDownload(peer: ActorRef, blockCount: Int)
  case class GotBlocks(peer: ActorRef, blocks: List[Block])
  case class DownloadTimeout()
}

class SPVBlockDownloader(blockchain: ActorRef,
  peerManager: ActorRef,
  networkParams: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import SPVBlockDownloader._

  implicit val timeout = Timeout(1 second)

  def receive: Receive = ready

  def ready: Receive = {
    case StartDownload(peer, blockCount) =>
      downloadFromPeer(peer, blockCount)
  }

  def downloading(peer: ActorRef, blockCount: Int, timeout: Cancellable): Receive = {
    case GotBlocks(p, hdrs) if p == peer =>
      timeout.cancel
      saveHeaders(hdrs)
      getChainHeight.map { h =>
        if (h >= blockCount)
          context.become(ready)
        if (hdrs.isEmpty)
          downloadFromRandomPeer(blockCount)
        else
          downloadFromPeer(peer, blockCount)
      }
    case DownloadTimeout() =>
      downloadFromRandomPeer(blockCount)
  }

  def saveHeaders(hdrs: List[Block]) = {
    for (hdr <- hdrs)
      blockchain ! BlockChain.PutBlock(hdr)
  }

  def downloadFromRandomPeer(blockCount: Int) =
    getRandomPeer.onSuccess {
      case Some(conn) =>
        downloadFromPeer(conn, blockCount)
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

  def requestBlocks(ref: ActorRef): Unit = {
    getBlockLocator.map(bl =>
      PeerConnection.Outgoing(
        GetHeaders(networkParams.PROTOCOL_VERSION, bl)))
      .pipeTo(ref)
  }

  def getChainHeight: Future[Int] = {
    (blockchain ? BlockChain.GetChainHead())(1 second)
      .mapTo[StoredBlock]
      .map(_.height)
  }

  def getRandomPeer: Future[Option[ActorRef]] =
    //(peerManager ? PeerManager.GetRandomConnection())
    //  .mapTo[Option[ActorRef]]
    Future.failed(new Exception())
}