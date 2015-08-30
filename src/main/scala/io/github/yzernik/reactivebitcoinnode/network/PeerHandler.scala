package io.github.yzernik.reactivebitcoinnode.network

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import PeerHandler.Initialize
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Addr
import io.github.yzernik.bitcoinscodec.messages.Alert
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.messages.GetAddr
import io.github.yzernik.bitcoinscodec.messages.GetData
import io.github.yzernik.bitcoinscodec.messages.Headers
import io.github.yzernik.bitcoinscodec.messages.Inv
import io.github.yzernik.bitcoinscodec.messages.Ping
import io.github.yzernik.bitcoinscodec.messages.Pong
import io.github.yzernik.bitcoinscodec.structures.InvVect
import io.github.yzernik.bitcoinscodec.structures.Message
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.reactivebitcoinnode.blockchain.BlockchainModule
import io.github.yzernik.reactivebitcoinnode.node.NetworkParameters

object PeerHandler {
  def props(blockchainController: ActorRef, peerManager: ActorRef, blockDownloader: ActorRef, networkParameters: NetworkParameters) =
    Props(classOf[PeerHandler], blockchainController, peerManager, blockDownloader, networkParameters)

  case class Initialize(conn: ActorRef, inbound: Boolean)

}

class PeerHandler(val blockchainController: ActorRef, val peerManager: ActorRef, blockDownloader: ActorRef, networkParameters: NetworkParameters)
    extends Actor with ActorLogging
    with BlockchainModule
    with NetworkModule {
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
        addr.addrs.foreach {
          case (_, addr) =>
            addNode(addr.address, false)
        }

      case headers: Headers =>
        blockDownloader ! headers

      case _: GetAddr =>
        sendMessage(getAddr, conn)

      case ping: Ping =>
        sendMessage(Pong(ping.nonce), conn)

      case alert: Alert =>
        println(s"Alert: ${alert.comment}")
        relayMessage(alert, conn)

      case inv: Inv =>
        inv.invs.foreach { iv =>
          if (iv.inv_type == InvVect.MSG_BLOCK) {
            sendMessage(GetData(List(iv)), conn)
          }
        }
        relayMessage(inv, conn) // TODO: validate before relaying Inv.

      case block: Block =>
        proposeNewBlock(block)

      case other =>
        log.info(s"Peer Handler received message: $other")
    }
  }

}