package com.oohish.bitcoinakkanode.spv

import java.net.InetSocketAddress
import scala.BigInt
import org.joda.time.DateTime
import com.oohish.bitcoinakkanode.node.PeerMessageHandler
import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.NetworkAddress
import akka.actor.ActorRef
import akka.actor.Props
import com.oohish.bitcoinakkanode.wire.PeerConnection
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.Addr
import com.oohish.bitcoinakkanode.wire.PeerManager

object SPVHandler {
  def props(peerManager: ActorRef,
    blockchain: ActorRef,
    networkParams: NetworkParameters) =
    Props(classOf[SPVHandler], peerManager, blockchain, networkParams)
}

class SPVHandler(peerManager: ActorRef,
  blockchain: ActorRef,
  networkParams: NetworkParameters) extends PeerMessageHandler {

  def handlePeerMessage(msg: Message): Unit = msg match {
    case Addr(addrs) =>
      for (addr <- addrs)
        peerManager ! PeerManager.AddPeer(addr._2.address)
    case msg =>
    //println("handled msg: " + msg)
  }

  def getVersion(remote: InetSocketAddress, local: InetSocketAddress): Version =
    Version(
      networkParams.PROTOCOL_VERSION,
      BigInt(1),
      DateTime.now().getMillis() / 1000,
      NetworkAddress(BigInt(1), remote),
      NetworkAddress(BigInt(1), local),
      Util.genNonce,
      "/Satoshi:0.7.2/",
      1,
      true)

  def onPeerConnected(ref: ActorRef): Unit = {
    ref ! PeerConnection.Outgoing(GetAddr())
  }

}