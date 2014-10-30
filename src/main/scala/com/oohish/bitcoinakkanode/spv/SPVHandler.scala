package com.oohish.bitcoinakkanode.spv

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.blockchain.BlockChain.StoredBlock
import com.oohish.bitcoinakkanode.node.PeerMessageHandler
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout.durationToTimeout

object SPVHandler {
  def props(peerManager: ActorRef,
    blockchain: ActorRef,
    downloader: ActorRef,
    networkParams: NetworkParameters) =
    Props(classOf[SPVHandler], peerManager, blockchain, downloader, networkParams)
}

class SPVHandler(peerManager: ActorRef,
  blockchain: ActorRef,
  downloader: ActorRef,
  val networkParams: NetworkParameters) extends PeerMessageHandler {
  import context._

  override def services: BigInt = SPVNode.services

  override def height: Int = 1 //TODO: use blockchain

  override def relay: Boolean = SPVNode.relay

  override def handlePeerMessage(msg: Message, peer: ActorRef): Unit =
    msg match {
      case Addr(addrs) =>
        for (addr <- addrs)
          peerManager ! PeerManager.AddPeer(addr._2.address)
      case Headers(hdrs) =>
        for (hdr <- hdrs)
          blockchain ! BlockChain.PutBlock(hdr)
        (blockchain ? BlockChain.GetChainHead())(1 second)
          .mapTo[StoredBlock]
          .map(_.height)
          .foreach { h =>
            downloader ! SPVBlockDownloader.GotBlocks(peer, h)
          }
      case msg =>
    }

  override def onPeerConnected(ref: ActorRef, v: Version): Unit = {
    downloader ! SPVBlockDownloader.StartDownload(ref, v.start_height)
  }

}