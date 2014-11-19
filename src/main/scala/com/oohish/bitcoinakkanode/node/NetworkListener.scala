package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.GetBlocks
import com.oohish.bitcoinscodec.messages.GetData
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.messages.Inv
import com.oohish.bitcoinscodec.messages.Ping
import com.oohish.bitcoinscodec.messages.Tx
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.structures.NetworkAddress

object NetworkListener {
  def props(blockchain: ActorRef, peerManager: ActorRef) =
    Props(classOf[NetworkListener], blockchain, peerManager)
}

class NetworkListener(blockchain: ActorRef, peerManager: ActorRef)
  extends Actor with ActorLogging {

  def receive: Receive = {
    case Addr(addrs) =>
      addAddresses(addrs)
    case Inv(vects) =>
    case GetData(vects) =>
    case GetBlocks(version, blockLocator, hashStop) =>
    case GetHeaders(version, blockLocator, hashStop) =>
    case tx: Tx =>
    case block: Block =>
    case Headers(hdrs) =>
    case GetAddr() =>
    case Ping(nonce) =>
  }

  def addAddresses(addrs: List[(Long, NetworkAddress)]) = {
    for (addr <- addrs) {
      peerManager ! PeerManager.AddAddress(addr._2.address)
    }
  }

}