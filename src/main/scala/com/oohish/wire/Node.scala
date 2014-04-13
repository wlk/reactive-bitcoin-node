package com.oohish.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.peermessages.Addr
import com.oohish.peermessages.MessagePayload
import com.oohish.structures.int32_t
import com.oohish.structures.uint64_t

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object Node {
  def props(listener: ActorRef) =
    Props(classOf[Node], listener)

  def version = int32_t(60002)

  def services = uint64_t(BigInt(1))

  case class Incoming(peer: Peer, msg: MessagePayload)
  //case class Outgoing(peer: Peer, msg: MessagePayload)

}

class Node(listener: ActorRef) extends Actor with ActorLogging {
  import Node._
  import com.oohish.peermessages.Addr
  import PeerManager._

  implicit val timeout = Timeout(5 seconds)
  import context.dispatcher

  // start the peer manager
  val peerManager = context.actorOf(PeerManager.props(self))

  def hangleMsg: PartialFunction[MessagePayload, Unit] = {
    case Addr(addresses) => {
      val peers = addresses.seq.map { netAddr =>
        val addr = InetAddress.getByAddress(netAddr.ip.ip.toArray)
        val port = netAddr.port.n.toInt
        Peer(new InetSocketAddress(addr, port))
      }
      peerManager ! Discovered(peers)
    }
    case _ =>
  }

  def receive = {

    case msg: MessagePayload => {
      //log.debug("peer: " + i.peer + " sent: " + i.msg)
      listener forward msg
      hangleMsg(msg)
    }

    case other => {
      log.warning("got other: " + other)
    }
  }

}