package io.github.yzernik.reactivebitcoinnode.network

import java.net.InetSocketAddress

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
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.BTC.PeerInfo

/**
 * @author yzernik
 */
trait NetworkModule {

  implicit val timeout: Timeout

  //def blockDownloader: ActorRef

  def peerManager: ActorRef

  private def getConnections(implicit executor: scala.concurrent.ExecutionContext) =
    (peerManager ? PeerManager.GetConnections).mapTo[Map[ActorRef, Version]]

  def getConnectionCount(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections.map(_.size)

  def addNode(socketAddr: InetSocketAddress, connect: Boolean) =
    peerManager ! PeerManager.AddNode(socketAddr, connect)

  def getPeerInfos(implicit executor: scala.concurrent.ExecutionContext) =
    for {
      connections <- getConnections
      infos <- Future.sequence {
        connections.keys.map { ref =>
          (ref ? BTC.GetPeerInfo).mapTo[PeerInfo]
        }
      }
    } yield infos.toList

  def getAddresses(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections.map(_.values.toList.map { _.addr_recv.address })

  def getNetworkTime(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections
      .map { c =>
        if (c.isEmpty) 0
        else {
          val times = c.values.map(_.timestamp)
          times.sum / times.size
        }
      }

  def getAddr(implicit executor: scala.concurrent.ExecutionContext) =
    for {
      addrs <- getAddresses
      t <- getNetworkTime
    } yield Addr(addrs.map { a => (t.toLong, NetworkAddress(BigInt(1), a)) })

  def sendMessage(msg: Future[Message], conn: ActorRef)(implicit ec: ExecutionContext) =
    msg.map(BTC.Send).pipeTo(conn)

  def sendMessage(msg: Message, conn: ActorRef) =
    conn ! BTC.Send(msg)

  def relayMessage(msg: Message, from: ActorRef)(implicit executor: scala.concurrent.ExecutionContext) =
    getConnections.map { c =>
      c.keys.filter(_ != from)
        .foreach { peer =>
          peer ! BTC.Send(msg)
        }
    }

}