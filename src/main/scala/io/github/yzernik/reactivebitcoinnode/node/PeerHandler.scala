package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.messages.GetAddr
import io.github.yzernik.bitcoinscodec.messages.GetHeaders
import io.github.yzernik.bitcoinscodec.messages.Headers
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import akka.pattern.{ ask, pipe }
import java.net.InetSocketAddress
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress

object PeerHandler {
  def props(blockchainController: ActorRef, peerManager: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerHandler], blockchainController, peerManager, networkParameters)

  case class Initialize(conn: ActorRef, inbound: Boolean)

}

class PeerHandler(blockchainController: ActorRef, peerManager: ActorRef, networkParameters: NetworkParameters)
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
    case BTC.Received(addr: Addr) =>
      handleAddr(addr)
    case BTC.Received(headers: Headers) =>
      handleHeaders(headers, conn)
    case BTC.Received(getAddr: GetAddr) =>
      handleGetAddr(conn)
    case other =>
      log.info(s"Peer Handler received message: $other")
  }

  private def initialSync(conn: ActorRef, inbound: Boolean) = {
    // log.info(s"Doing initial sync with peer: $conn")
    conn ! BTC.Send(GetAddr())
    getGetHeaders.map(BTC.Send).pipeTo(conn)
  }

  private def getBlockLocator =
    (blockchainController ? BlockchainController.GetBlockLocator).mapTo[List[Hash]]

  /**
   * Get the GetHeaders message.
   */
  private def getGetHeaders =
    getBlockLocator.map { bl =>
      GetHeaders(networkParameters.PROTOCOL_VERSION, bl, Hash.NULL)
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
    headers.invs.foreach { block =>
      blockchainController ! BlockchainController.ProposeNewBlock(block)
    }
    if (!headers.invs.isEmpty)
      getGetHeaders.map(BTC.Send).pipeTo(conn)
  }

  /**
   * Handle a GetAddr message.
   */
  private def handleGetAddr(conn: ActorRef) = {
    val addresses = (peerManager ? PeerManager.GetAddresses).mapTo[List[InetSocketAddress]]
    val networkTime = (peerManager ? PeerManager.GetNetworkTime).mapTo[Int]
    val addr = for {
      addrs <- addresses
      t <- networkTime
    } yield Addr(addrs.map { a => (t.toLong, NetworkAddress(BigInt(1), a)) })
    getGetHeaders.map(BTC.Send).pipeTo(conn)
  }

}