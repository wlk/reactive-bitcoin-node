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
      sender ! "Busy syncing"
    case PeerManager.ReceivedMessage(Headers(hdrs), from) if from == conn =>
      log.info("received headers message")
      timeout.cancel
      if (hdrs.isEmpty)
        context.become(ready)
      else {
        hdrs.foreach {
          blockchain ! BlockChain.PutBlock(_)
        }
        val timeout = context.system.scheduler.
          scheduleOnce(10.second, self, SyncTimeout())
        context.become(syncing(conn, timeout))
        requestBlocks(conn)
      }
    case SyncTimeout() =>
      log.info("sync timeout")
      context.become(ready)
    case _ =>
  }

  def requestBlocks(ref: ActorRef) = {
    log.info("sending block locator")
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