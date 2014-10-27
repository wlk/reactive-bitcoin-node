package com.oohish.bitcoinakkanode.spv

import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinakkanode.node.APIClient
import com.oohish.bitcoinakkanode.node.HeadersDownloaderComponent
import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinakkanode.node.SPVBlockChainComponent
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.messages.Version

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(val networkParams: NetworkParameters) extends Actor with ActorLogging
  with Node
  with SPVBlockChainComponent
  with HeadersDownloaderComponent {
  import context.dispatcher

  implicit val timeout = Timeout(1 second)

  override def nodeBehavior: Receive = {
    case Headers(hdrs) =>
      hdrs.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
      if (!hdrs.isEmpty) {
        val peer = sender
        getChainHead
          .map(_.height)
          .map(SPVBlockDownloader.GotBlocks(peer, _))
          .pipeTo(downloader)
      }
  }

  override def syncWithPeer(peer: ActorRef, version: Version) = {
    downloader ! SPVBlockDownloader.StartDownload(peer, version.start_height)
  }

  override def services: BigInt = BigInt(1)

  override def getBlockChainHeight(): Future[Int] = Future.successful(1)

  override def relay: Boolean = false

}