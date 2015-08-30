package io.github.yzernik.reactivebitcoinnode.network

import java.net.InetSocketAddress

import scala.BigInt
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.language.postfixOps

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress
import io.github.yzernik.btcio.actors.BTC

/**
 * @author yzernik
 */
trait NetworkModule {

  implicit val timeout: Timeout

  //def blockDownloader: ActorRef

  def peerManager: ActorRef

  def getConnectionCount =
    (peerManager ? PeerManager.GetConnectionCount).mapTo[Int]

  def addNode(socketAddr: InetSocketAddress, connect: Boolean) =
    peerManager ! PeerManager.AddNode(socketAddr, connect)

  def getAddresses =
    (peerManager ? PeerManager.GetAddresses).mapTo[List[InetSocketAddress]]

  def getNetworkTime =
    (peerManager ? PeerManager.GetNetworkTime).mapTo[Long]

  /**
   * Get the Addr message to send to a peer.
   */
  def getAddr(implicit executor: scala.concurrent.ExecutionContext) =
    for {
      addrs <- getAddresses
      t <- getNetworkTime
    } yield Addr(addrs.map { a => (t.toLong, NetworkAddress(BigInt(1), a)) })

  def sendMessage(msg: Future[Message], conn: ActorRef)(implicit ec: ExecutionContext) =
    msg.map(BTC.Send).pipeTo(conn)

  def sendMessage(msg: Message, conn: ActorRef) =
    conn ! BTC.Send(msg)

  def relayMessage(msg: Message, from: ActorRef) =
    peerManager ! PeerManager.RelayMessage(msg, from)

  def getPeerInfo =
    (peerManager ? PeerManager.GetPeerInfo).mapTo[List[BTC.PeerInfo]]

}