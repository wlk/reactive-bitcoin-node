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
        .map("Busy syncing with " + _.height + " downloaded blocks.")
        .pipeTo(sender)
    case PeerManager.ReceivedMessage(Headers(hdrs), from) if from == conn =>
      timeout.cancel
      if (hdrs.isEmpty)
        context.become(ready)
      else {
        hdrs.foreach {
          blockchain ! BlockChain.PutBlock(_)
        }
        val t = context.system.scheduler.
          scheduleOnce(10.second, self, SyncTimeout())
        context.become(syncing(conn, t))
        requestBlocks(conn)
      }
    case SyncTimeout() =>
      val t = context.system.scheduler.
        scheduleOnce(10.second, self, SyncTimeout())
      context.become(syncing(conn, t))
      requestBlocks(conn)
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

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}