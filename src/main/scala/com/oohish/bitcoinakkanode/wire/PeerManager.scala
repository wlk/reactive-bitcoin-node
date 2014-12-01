package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.util.Util.currentSeconds
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object PeerManager {
  def props(addressManager: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], addressManager, networkParameters)

  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress, v: Version)
  case class GetPeers()

}

class PeerManager(addressManager: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context._
  import PeerManager._

  var peers = Map.empty[ActorRef, (Long, NetworkAddress)]

  def receive: Receive = {
    case PeerConnected(ref, addr, v) =>
      val offset = v.timestamp - currentSeconds
      //val networkAddress = NetworkAddress(v.services, addr)
      //peers += ref -> (offset, networkAddress)
      //context.watch(ref)
      //ref ! PeerConnection.Outgoing(GetAddr())
      log.info("peer connected: {}", addr)
    case akka.actor.Terminated(ref) =>
      peers -= ref
    case GetPeers() =>
      sender ! peers.values.toList
  }

  /*
   * Get the network-adjusted time.
   */
  def networkTime = currentSeconds + medianOffset

  /*
   * Get the median offset from the local nodes clock time in seconds.
   */
  def medianOffset: Long = {
    if (peers.isEmpty) 0
    else {
      val offsets = peers.values.map(_._1).toList
      val medianIndex = offsets.length / 2
      offsets(medianIndex)
    }
  }

}