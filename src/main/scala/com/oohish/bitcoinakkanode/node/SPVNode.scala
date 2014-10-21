package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.node.Node.GetBestBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetBlockCount
import com.oohish.bitcoinakkanode.node.Node.GetBlockHash
import com.oohish.bitcoinakkanode.node.Node.SyncTimeout
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.GetHeaders
import com.oohish.bitcoinscodec.messages.Headers
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node {
  import context.dispatcher
  import com.oohish.bitcoinakkanode.node.Node._

  def networkParams = np

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))

  def receive = ready

  def ready: Receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      pm ! PeerManager.UnicastMessage(GetAddr(), ref)
      requestBlocks(ref)
    case PeerManager.ReceivedMessage(msg, from) =>
      msgReceive(from)(msg)
    case cmd: APICommand =>
      (receiveNetworkCommand orElse receiveSPVCommand)(cmd)
  }

  def syncing(conn: ActorRef, timeout: Cancellable): Receive = {
    case PeerManager.ReceivedMessage(Headers(hdrs), from) if from == conn =>
      timeout.cancel
      if (hdrs.isEmpty)
        context.become(ready)
      else {
        hdrs.foreach {
          blockchain ! BlockChain.PutBlock(_)
        }
        syncWithPeer(conn)
      }
    case SyncTimeout() =>
      (pm ? PeerManager.GetRandomConnection())(Timeout(5 seconds))
        .mapTo[Option[ActorRef]]
        .onSuccess {
          case Some(conn) =>
            syncWithPeer(conn)
        }
    case cmd: APICommand =>
      (receiveNetworkCommand orElse receiveSPVCommand)(cmd)
    case _ =>
  }

  def receiveSPVCommand: PartialFunction[APICommand, Unit] = {
    case GetBestBlockHash() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[BlockChain.StoredBlock]
        .map(_.hash)
        .pipeTo(sender)
    case GetBlockCount() =>
      (blockchain ? BlockChain.GetChainHead())
        .mapTo[BlockChain.StoredBlock]
        .map(_.height)
        .pipeTo(sender)
    case GetBlockHash(index) =>
      (blockchain ? BlockChain.GetBlockByIndex(index))
        .mapTo[Option[BlockChain.StoredBlock]]
        .map(_.map(_.hash))
        .pipeTo(sender)
  }

  def requestBlocks(ref: ActorRef) = {
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[List[Hash]]
      .map(bl =>
        PeerManager.UnicastMessage(
          GetHeaders(networkParams.PROTOCOL_VERSION, bl), ref))
      .pipeTo(pm)
  }

  def syncWithPeer(peer: ActorRef) = {
    val t = context.system.scheduler.
      scheduleOnce(5.second, self, SyncTimeout())
    context.become(syncing(peer, t))
    requestBlocks(peer)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Headers(hdrs) =>
      if (!hdrs.isEmpty) {
        hdrs.foreach {
          blockchain ! BlockChain.PutBlock(_)
        }
        syncWithPeer(from)
      }
    case _ =>
  }

}