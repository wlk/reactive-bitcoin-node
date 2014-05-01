package com.oohish.wire

import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.Verack
import com.oohish.peermessages.Version
import com.oohish.structures.int64_t
import com.oohish.wire.PeerManager.PeerConnected
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import com.oohish.structures.int32_t
import org.joda.time.DateTime
import com.oohish.structures.VarStr
import com.oohish.structures.NetworkAddress
import com.oohish.structures.uint64_t
import com.oohish.structures.IP
import com.oohish.structures.Port
import scala.util.Random
import java.net.InetSocketAddress
import java.net.InetAddress

object BTCConnection {
  def props(peer: Peer, networkParams: NetworkParameters, node: ActorRef, manager: ActorRef) =
    Props(classOf[BTCConnection], peer, networkParams, node, manager)

  case class ConnectTimeout()
  case class Outgoing(m: MessagePayload)

  def verack = Verack()

  def version(networkParams: NetworkParameters, peer: Peer) = Version(
    versionNum(networkParams),
    Node.services,
    int64_t(DateTime.now().getMillis()),
    peerNetworkAddress(peer),
    myNetworkAddress,
    genNonce,
    VarStr("/Satoshi:0.7.2/"),
    int32_t(1))

  def versionNum(networkParams: NetworkParameters): int32_t =
    int32_t(networkParams.PROTOCOL_VERSION.n.toInt)

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

  val selfPeer = Peer(new InetSocketAddress(InetAddress.getLocalHost(), 8333))

}

class BTCConnection(peer: Peer, networkParams: NetworkParameters, node: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  import BTCConnection._
  import akka.actor.Terminated
  import Node.Incoming

  context.parent ! BTCConnection.version(networkParams, peer)

  def receive = connecting(false, None)

  def connecting(verackReceived: Boolean, versionReceived: Option[int64_t]): Receive = {

    case _: Verack => {
      if (versionReceived.isDefined) {
        finishHandshake(versionReceived.get)
      } else {
        context.become(connecting(true, None))
      }
    }

    case m: Version => {
      context.parent ! BTCConnection.verack
      if (verackReceived) {
        finishHandshake(m.timestamp)
      } else {
        context.parent ! BTCConnection.version(networkParams, peer)
        context.become(connecting(false, Some(m.timestamp)))
      }
    }

    case m: ConnectTimeout => {
      context.stop(self)
    }

    case other => {
      log.debug("BTCConnection got other: " + other)
    }

  }

  def finishHandshake(time: int64_t): Unit = {
    manager ! PeerConnected(peer, time.n)
    node ! Verack()
    log.info("becoming connected")
    context.become(connected)
  }

  def connected(): Receive = {

    case Outgoing(m) => {
      log.debug("outgoing message: " + m)
      context.parent ! m
    }

    case m: MessagePayload => {
      node ! m
    }

    case Terminated(ref) => {
      context.stop(self)
    }

    case other => {
      log.warning("BTCConnection got other: " + other)
    }

  }

}