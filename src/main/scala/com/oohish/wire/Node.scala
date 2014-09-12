package com.oohish.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import com.oohish.bitcoinscodec.structures.Message._
import com.oohish.bitcoinscodec.messages._
import com.oohish.wire.BTCConnection.Outgoing
import PeerManager.Discovered
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.Timeout
import scodec.bits.ByteVector

import com.oohish.bitcoinscodec.structures.InvVect._

object Node {
  def props(
    networkParams: NetworkParameters,
    spv: Boolean = false) =
    Props(classOf[Node], networkParams, spv)

  def services = BigInt(1)

  case class Incoming(peer: Peer, msg: Message)
  //case class Outgoing(peer: Peer, msg: MessagePayload)

}

class Node(
  networkParams: NetworkParameters,
  spv: Boolean) extends Actor with ActorLogging {
  import Node._
  import PeerManager._

  implicit val timeout = Timeout(5 seconds)
  import context.dispatcher

  //start the blockchain
  /*
  val blockchain =
    context.actorOf(SPVBlockChain.props(networkParams))
   */

  // start the peer manager
  val peerManager = context.actorOf(PeerManager.props(self, networkParams))

  def receive = {

    case Addr(timeaddrs) => {
      val peers = timeaddrs.map { timeaddr =>
        val addr = InetAddress.getByAddress(timeaddr._2.address.left.get.value.toArray)
        val port = timeaddr._2.port.value
        Peer(new InetSocketAddress(addr, port))
      }
      peerManager ! Discovered(peers)
    }

    case Inv(vectors) => {
      val txVectors = vectors.filter { inv =>
        inv.inv_type == MSG_TX
      }

      /*
      val blockVectors = vectors.filter { inv =>
        inv.t.name == "MSG_BLOCK"
      }
       */

      sender ! Outgoing(GetData(txVectors))
      //sender ! Outgoing(GetData(blockVectors))

      //blockchain forward Inv(vectors)
    }

    case tx: Tx => {
      // https://en.bitcoin.it/wiki/Protocol_rules#.22tx.22_messages

      //log.debug("received Tx!!!!!!!!!!!")

      // Make sure neither in or out lists are empty

      // Size in bytes < MAX_BLOCK_SIZE

      // Each output value, as well as the total, must be in legal money range

      // Make sure none of the inputs have hash=0, n=-1 (coinbase transactions)

    }

    case blk: Block => {
      //blockchain forward blk
    }

    case msg: Message => {
      //log.info("received: " + msg.getClass().getName())
      //blockchain forward msg
    }

    case other => {
      log.info("Node got other: " + other)
    }
  }

}