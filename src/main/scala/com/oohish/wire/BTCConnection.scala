package com.oohish.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.Array.canBuildFrom
import scala.math.BigInt.int2bigInt
import scala.util.Random

import org.joda.time.DateTime

import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.Verack
import com.oohish.peermessages.Version
import com.oohish.structures.IP
import com.oohish.structures.NetworkAddress
import com.oohish.structures.Port
import com.oohish.wire.PeerManager.PeerConnected

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala

object BTCConnection {
  def props(peer: Peer, networkParams: NetworkParameters, node: ActorRef, manager: ActorRef) =
    Props(classOf[BTCConnection], peer, networkParams, node, manager)

  case class ConnectTimeout()
  case class Outgoing(m: MessagePayload)

  def verack = Verack()

  def version(networkParams: NetworkParameters, peer: Peer) = Version(
    versionNum(networkParams),
    Node.services,
    DateTime.now().getMillis(),
    peerNetworkAddress(peer),
    myNetworkAddress,
    genNonce,
    "/Satoshi:0.7.2/",
    1)

  def versionNum(networkParams: NetworkParameters): Int =
    networkParams.PROTOCOL_VERSION.toInt

  def peerNetworkAddress(peer: Peer) = {
    NetworkAddress(
      1,
      IP(peer.address.getAddress().getHostAddress()),
      Port(peer.port))
  }

  def myNetworkAddress = peerNetworkAddress(selfPeer)

  def genNonce(): BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

  val selfPeer = Peer(new InetSocketAddress(InetAddress.getLocalHost(), 8333))

}

class BTCConnection(peer: Peer, networkParams: NetworkParameters, node: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  import BTCConnection._
  import akka.actor.Terminated
  import Node.Incoming

  context.parent ! BTCConnection.version(networkParams, peer)

  def receive = connecting(false, None)

  def connecting(verackReceived: Boolean, versionReceived: Option[Long]): Receive = {

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

  def finishHandshake(time: Long): Unit = {
    manager ! PeerConnected(peer, time)
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