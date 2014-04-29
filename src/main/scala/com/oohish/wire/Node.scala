package com.oohish.wire

import java.net.InetAddress

import java.net.InetSocketAddress
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import org.joda.time.DateTime
import com.oohish.chain.SPVBlockChain
import com.oohish.peermessages.Addr
import com.oohish.peermessages.GetData
import com.oohish.peermessages.Inv
import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.Tx
import com.oohish.peermessages.Verack
import com.oohish.peermessages.Version
import com.oohish.structures.IP
import com.oohish.structures.InvVect
import com.oohish.structures.NetworkAddress
import com.oohish.structures.Port
import com.oohish.structures.VarStr
import com.oohish.structures.VarStruct
import com.oohish.structures.int32_t
import com.oohish.structures.int64_t
import com.oohish.structures.uint64_t
import com.oohish.wire.BTCConnection.Outgoing
import PeerManager.Discovered
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.Timeout
import com.oohish.chain.FullBlockChain
import com.oohish.peermessages.Block

import reactivemongo.api._

object Node {
  def props(
    networkParams: NetworkParameters,
    spv: Boolean = false,
    conn: Option[MongoConnection] = None) =
    Props(classOf[Node], networkParams, spv, conn)

  val selfPeer = Peer(new InetSocketAddress(InetAddress.getLocalHost(), 8333))

  def verack = Verack()

  def version(peer: Peer) = Version(
    Node.versionNum,
    Node.services,
    int64_t(DateTime.now().getMillis()),
    peerNetworkAddress(peer),
    myNetworkAddress,
    genNonce,
    VarStr("/Satoshi:0.7.2/"),
    int32_t(1))

  def peerNetworkAddress(peer: Peer) = {
    NetworkAddress(
      uint64_t(BigInt(1)),
      IP(peer.address.getAddress().getHostAddress()),
      Port(peer.port))
  }

  def myNetworkAddress = peerNetworkAddress(selfPeer)

  def genNonce(): uint64_t = {
    val n = new Random().nextLong
    uint64_t(uint64_t.asBigInt(n))
  }

  def versionNum = int32_t(60002)

  def services = uint64_t(BigInt(1))

  case class Incoming(peer: Peer, msg: MessagePayload)
  //case class Outgoing(peer: Peer, msg: MessagePayload)

}

class Node(
  networkParams: NetworkParameters,
  spv: Boolean,
  conn: Option[MongoConnection]) extends Actor with ActorLogging {
  import Node._
  import com.oohish.peermessages.Addr
  import PeerManager._

  implicit val timeout = Timeout(5 seconds)
  import context.dispatcher

  //start the blockchain
  val blockchain =
    if (spv) {
      context.actorOf(SPVBlockChain.props(networkParams))
    } else {
      context.actorOf(FullBlockChain.props(networkParams, conn))
    }

  // start the peer manager
  val peerManager = context.actorOf(PeerManager.props(self, networkParams))

  def receive = {

    case Addr(addresses) => {
      val peers = addresses.map { tNetAddr =>
        val addr = InetAddress.getByAddress(tNetAddr.addr.ip.ip.toArray)
        val port = tNetAddr.addr.port.n.toInt
        Peer(new InetSocketAddress(addr, port))
      }
      peerManager ! Discovered(peers)
    }

    case Inv(vectors) => {

      log.info("Node received Inv")

      val txVectors = vectors.filter { inv =>
        inv.t.name == "MSG_TX"
      }

      val blockVectors = vectors.filter { inv =>
        inv.t.name == "MSG_BLOCK"
      }

      sender ! Outgoing(GetData(txVectors))
      sender ! Outgoing(GetData(blockVectors))
    }

    case tx: Tx => {
      // https://en.bitcoin.it/wiki/Protocol_rules#.22tx.22_messages

      log.info("received Tx!!!!!!!!!!!")

      // Make sure neither in or out lists are empty

      // Size in bytes < MAX_BLOCK_SIZE

      // Each output value, as well as the total, must be in legal money range

      // Make sure none of the inputs have hash=0, n=-1 (coinbase transactions)

    }

    case blk: Block => {
      log.info("received block!!!!!!!!")
      blockchain forward blk
    }

    case msg: MessagePayload => {
      log.info("received: " + msg.getClass().getName())
      blockchain forward msg
    }

    case other => {
      log.info("Bode got other: " + other)
    }
  }

}