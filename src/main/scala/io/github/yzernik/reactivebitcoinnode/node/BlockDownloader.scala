package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.joda.time.DateTime

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
import io.github.yzernik.bitcoinscodec.messages.GetHeaders
import io.github.yzernik.bitcoinscodec.messages.Headers
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC

object BlockDownloader {
  def props(blockchainController: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[BlockDownloader], blockchainController, networkParameters)

  case class SyncWithPeer(conn: ActorRef)
  case object DownloadRequestTimeout
}

class BlockDownloader(blockchainController: ActorRef, networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import BlockDownloader._
  import context.dispatcher
  import context.system

  import akka.actor.Actor
  import akka.actor.Props
  import scala.concurrent.duration._

  implicit val timeout = Timeout(10 seconds)

  var downloadPeers = Map.empty[ActorRef, Long]

  def receive = idle

  def idle: Receive = {
    case SyncWithPeer(conn) =>
      downloadPeers += conn -> 0
      updateDownloader
  }

  def downloading(peer: ActorRef, time: DateTime, timeout: Cancellable): Receive = {
    case headers: Headers =>
      timeout.cancel
      updateResponseTime(peer, time)
      handleHeaders(peer, headers)
      updateDownloader
    case DownloadRequestTimeout =>
      updateResponseTime(peer, time)
      updateDownloader
    case SyncWithPeer(conn) =>
      downloadPeers += conn -> 0
  }

  private def updateResponseTime(peer: ActorRef, startTime: DateTime) = {
    val requestTime = DateTime.now.getMillis - startTime.getMillis
    downloadPeers += peer -> requestTime
  }

  /**
   * Handle a Headers message.
   */
  private def handleHeaders(peer: ActorRef, headers: Headers) = {
    if (headers.invs.isEmpty) downloadPeers -= peer
    headers.invs.foreach { h =>
      blockchainController ! BlockchainController.ProposeNewBlock(h)
    }
  }

  /**
   * Start downloading from the fastest peer.
   */
  private def updateDownloader = {
    val timeout = system.scheduler.scheduleOnce(10 seconds, self, DownloadRequestTimeout)
    val p = getFastestPeer
    if (p.isDefined) {
      getGetHeaders.map(BTC.Send).pipeTo(p.get)
      context.become(downloading(p.get, DateTime.now, timeout))
    } else
      context.become(idle)
  }

  /**
   * Get the block locator hashes.
   */
  private def getBlockLocator =
    (blockchainController ? BlockchainController.GetBlockLocator)((5 seconds)).mapTo[List[Hash]]

  /**
   * Get the GetHeaders message.
   */
  private def getGetHeaders =
    getBlockLocator.map { bl =>
      GetHeaders(networkParameters.PROTOCOL_VERSION, bl, Hash.NULL)
    }

  /**
   * Get the fastest download peer.
   */
  private def getFastestPeer = {
    val sortedPeers = downloadPeers.toSeq.sortBy(_._2)
    sortedPeers.headOption.map(_._1)
  }

}