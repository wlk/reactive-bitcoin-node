package com.oohish.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.chain.SPVBlockChain
import com.oohish.peermessages.Addr
import com.oohish.peermessages.GetData
import com.oohish.peermessages.Inv
import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.Tx
import com.oohish.structures.InvVect
import com.oohish.structures.VarStruct
import com.oohish.structures.int32_t
import com.oohish.structures.uint64_t
import com.oohish.wire.BTCConnection.Outgoing

import PeerManager.Discovered
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.util.Timeout

object Node {
  def props() =
    Props(classOf[Node])

  def version = int32_t(60002)

  def services = uint64_t(BigInt(1))

  case class Incoming(peer: Peer, msg: MessagePayload)
  //case class Outgoing(peer: Peer, msg: MessagePayload)

}

class Node() extends Actor with ActorLogging {
  import Node._
  import com.oohish.peermessages.Addr
  import PeerManager._

  implicit val timeout = Timeout(5 seconds)
  import context.dispatcher

  //start the header chain store
  val chainStore = context.actorOf(SPVBlockChain.props)

  // start the peer manager
  val peerManager = context.actorOf(PeerManager.props(self))

  def receive = {

    case Addr(addresses) => {
      val peers = addresses.seq.map { netAddr =>
        val addr = InetAddress.getByAddress(netAddr.ip.ip.toArray)
        val port = netAddr.port.n.toInt
        Peer(new InetSocketAddress(addr, port))
      }
      peerManager ! Discovered(peers)
    }

    case Inv(vectors) => {

      log.info("Node received Inv")

      val txVectors = vectors.seq.filter { inv =>
        inv.t.name == "MSG_TX"
      }

      sender ! Outgoing(GetData(VarStruct[InvVect](txVectors)))
    }

    case tx: Tx => {
      // https://en.bitcoin.it/wiki/Protocol_rules#.22tx.22_messages

      log.info("received Tx!!!!!!!!!!!")

      // Make sure neither in or out lists are empty

      // Size in bytes < MAX_BLOCK_SIZE

      // Each output value, as well as the total, must be in legal money range

      // Make sure none of the inputs have hash=0, n=-1 (coinbase transactions)

    }

    case msg: MessagePayload => {
      log.info("received: " + msg.getClass().getName())
      chainStore forward msg
    }

    case other => {
      log.info("got other: " + other)
    }
  }

}