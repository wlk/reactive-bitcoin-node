package com.oohish.bitcoinakkanode.wire

import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinscodec.messages.Alert
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.GetBlocks
import com.oohish.bitcoinscodec.messages.GetData
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.messages.Inv
import com.oohish.bitcoinscodec.messages.MemPool
import com.oohish.bitcoinscodec.messages.NotFound
import com.oohish.bitcoinscodec.messages.Ping
import com.oohish.bitcoinscodec.messages.Pong
import com.oohish.bitcoinscodec.messages.Reject
import com.oohish.bitcoinscodec.messages.Tx
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object PeerHandler {
  def props(blockchain: ActorRef, addressManager: ActorRef, peerConnection: ActorRef) =
    Props(classOf[PeerHandler], blockchain, addressManager, peerConnection)
}

class PeerHandler(blockchain: ActorRef, addressManager: ActorRef, peerConnection: ActorRef) extends Actor with ActorLogging {

  peerConnection ! PeerConnection.Register(self)

  def receive = {
    case addr: Addr =>
      for (addr <- addr.addrs)
        addressManager ! AddressManager.AddAddress(addr._2.address)
    case alert: Alert =>
    case block: Block =>
    case getAddr: GetAddr =>
    case getBlocks: GetBlocks =>
    case getData: GetData =>
    case getHeaders: GetHeaders =>
    case headers: Headers =>
    case inv: Inv =>
    case memPool: MemPool =>
    case notFound: NotFound =>
    case ping: Ping =>
    case pong: Pong =>
    case reject: Reject =>
    case tx: Tx =>
    case verack: Verack =>
    case version: Version =>
  }

}