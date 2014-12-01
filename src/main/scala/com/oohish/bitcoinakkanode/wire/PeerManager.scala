package com.oohish.bitcoinakkanode.wire

import java.net.InetSocketAddress

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinakkanode.util.Util.currentSeconds
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object PeerManager {
  def props(blockchain: ActorRef,
    addressManager: ActorRef,
    networkParameters: NetworkParameters) =
    Props(classOf[PeerManager], blockchain, addressManager, networkParameters)

  case class PeerConnected(ref: ActorRef, addr: InetSocketAddress, v: Version)
  case class GetPeers()
}

class PeerManager(blockchain: ActorRef,
  addressManager: ActorRef,
  networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context._
  import PeerManager._

  var peers = Map.empty[ActorRef, (Long, NetworkAddress)]

  def receive: Receive = {
    case PeerConnected(pc, addr, version) =>
      context.watch(pc)
      val offset = getPeerOffset(version)
      //ref ! PeerConnection.Outgoing(GetAddr())
      syncWithPeer(pc)
      val handler = context.actorOf(PeerHandler.props(blockchain, addressManager, pc))
      val networkAddress = NetworkAddress(version.services, addr)
      log.info("pc added: {}", pc)
      peers += pc -> (offset, networkAddress)
      log.info("number of peers connected: {}", peers.size)
    case akka.actor.Terminated(ref) =>
      log.info("pc terminated: {}", ref)
      peers -= ref
      log.info("number of peers connected: {}", peers.size)
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
    if (!peers.isEmpty) {
      val offsets = peers.values.map(_._1).toList
      val medianIndex = offsets.length / 2
      offsets(medianIndex)
    } else {
      0
    }
  }

  /*
   * Get the set of connected addresses.
   */
  def getConnectedAddresses = peers.values.map(_._2.address).toSet

  /*
   * Get the offset of the connected peer.
   */
  def getPeerOffset(version: Version) = version.timestamp - Util.currentSeconds

  /*
   * Sync with newly connected peer.
   */
  def syncWithPeer(pc: ActorRef) = {
    val syncer = context.actorOf(PeerSyncer.props(blockchain, addressManager, self))
    syncer ! PeerSyncer.SyncWithPeer(pc)
  }

}