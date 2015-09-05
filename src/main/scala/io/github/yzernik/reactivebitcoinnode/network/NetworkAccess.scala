package io.github.yzernik.reactivebitcoinnode.network

import java.net.InetSocketAddress
import akka.actor.ActorRef
import io.github.yzernik.btcio.actors.BTC.PeerInfo
import scala.concurrent.Future
import scala.BigInt
import scala.annotation.migration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.postfixOps
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.bitcoinscodec.structures.Message

/**
 * @author yzernik
 */
trait NetworkAccess {

  implicit val timeout: Timeout

  def getConnections(implicit executor: scala.concurrent.ExecutionContext): Future[Set[ActorRef]]

  def addNode(socketAddr: InetSocketAddress, connect: Boolean): Unit

  def getPeerInfos(implicit executor: scala.concurrent.ExecutionContext) =
    for {
      connections <- getConnections
      infos <- Future.sequence {
        connections.toList.map { ref =>
          (ref ? BTC.GetPeerInfo).mapTo[PeerInfo]
        }
      }
    } yield infos

  def getAddresses(implicit executor: scala.concurrent.ExecutionContext) =
    for {
      peer <- getPeerInfos
    } yield peer.map(_.addr)

  def getNetworkTime(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections.map {
      c => 0
    }

  def relayMessage(msg: Message, from: ActorRef)(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections.map { c =>
      c.filter(_ != from)
        .foreach { peer =>
          peer ! BTC.Send(msg)
        }
    }

}