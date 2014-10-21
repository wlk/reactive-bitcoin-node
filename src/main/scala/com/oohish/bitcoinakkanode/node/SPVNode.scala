package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.node.Node.SyncTimeout
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
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
import scala.util.Success

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(np: NetworkParameters) extends Node {
  import context.dispatcher
  import com.oohish.bitcoinakkanode.node.Node._

  def networkParams = np

  lazy val blockchain = context.actorOf(SPVBlockChain.props(networkParams))

  def syncing(conn: ActorRef, timeout: Cancellable): Receive = {
    case cmd: APICommand =>
      (blockchain ? BlockChain.GetChainHead())(Timeout(5 seconds))
        .mapTo[BlockChain.StoredBlock]
        .map("Node is busy synching blockchain." + " " + _.height + " blocks downloaded.")
        .pipeTo(sender)
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
          case rc => {
            log.info("syncing with rc")
            rc.foreach(syncWithPeer)
          }
        }
    case _ =>
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
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}