package io.github.yzernik.reactivebitcoinnode.node

import java.net.InetSocketAddress

import scala.BigInt
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.messages.Alert
import io.github.yzernik.bitcoinscodec.messages.GetAddr
import io.github.yzernik.bitcoinscodec.messages.Headers
import io.github.yzernik.bitcoinscodec.messages.Ping
import io.github.yzernik.bitcoinscodec.messages.Pong
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress
import io.github.yzernik.btcio.actors.BTC

object PeerHandler {
  def props(blockchainController: ActorRef, peerManager: ActorRef, blockDownloader: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerHandler], blockchainController, peerManager, blockDownloader, networkParameters)

  case class Initialize(conn: ActorRef, inbound: Boolean)

}

class PeerHandler(blockchainController: ActorRef, peerManager: ActorRef, blockDownloader: ActorRef, networkParameters: NetworkParameters)
  extends Actor with ActorLogging {
  import PeerHandler._
  import context.dispatcher

  implicit val timeout = Timeout(5 seconds)

  def receive: Receive = {
    case Initialize(conn, inbound) =>
      context.watch(conn)
      context.become(ready(conn))
      conn ! BTC.Register(self)
      initialSync(conn, inbound)
  }

  def ready(conn: ActorRef): Receive = {
    case BTC.Closed =>
      context.stop(self)
    case Terminated(ref) =>
      context.stop(self)
    case BTC.Received(msg) =>
      handleMsg(msg, conn)
  }

  /**
   * Do the initial sync with a new peer connection.
   */
  private def initialSync(conn: ActorRef, inbound: Boolean) = {
    conn ! BTC.Send(GetAddr())
    blockDownloader ! BlockDownloader.SyncWithPeer(conn)
  }

  /**
   * Handle an incoming message from a peer.
   */
  private def handleMsg(msg: Message, conn: ActorRef) = {
    msg match {
      case addr: Addr =>
        handleAddr(addr)
      case headers: Headers =>
        handleHeaders(headers, conn)
      case getAddr: GetAddr =>
        handleGetAddr(conn)
      case ping: Ping =>
        handlePing(ping, conn)
      case alert: Alert =>
        handleAlert(alert, conn)
      case other =>
        log.info(s"Peer Handler received message: $other")
    }
  }

  /**
   * Handle an Addr message from a peer.
   */
  private def handleAddr(addr: Addr) =
    addr.addrs.foreach { addr =>
      val socketAddr = addr._2.address
      peerManager ! PeerManager.AddNode(socketAddr, false)
    }

  /**
   * Handle a Headers message.
   */
  private def handleHeaders(headers: Headers, conn: ActorRef) = {
    blockDownloader ! headers
  }

  /**
   * Handle a GetAddr message.
   */
  private def handleGetAddr(conn: ActorRef) = {
    getAddr.map(BTC.Send).pipeTo(conn)
  }

  /**
   * Get the Addr message to send to a peer.
   */
  private def getAddr =
    for {
      addrs <- (peerManager ? PeerManager.GetAddresses).mapTo[List[InetSocketAddress]]
      t <- (peerManager ? PeerManager.GetNetworkTime).mapTo[Int]
    } yield Addr(addrs.map { a => (t.toLong, NetworkAddress(BigInt(1), a)) })

  /**
   * Handle a Ping message.
   */
  private def handlePing(ping: Ping, conn: ActorRef) = {
    conn ! BTC.Send(Pong(ping.nonce))
  }

  /**
   * Handle an Alert message.
   */
  private def handleAlert(alert: Alert, conn: ActorRef) = {
    println(s"Alert: ${alert.comment}")
    peerManager ! PeerManager.RelayMessage(alert, conn)
  }

}