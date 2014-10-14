package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import com.oohish.bitcoinakkanode.node.BlockChain.GetBlockLocatorResponse
import com.oohish.bitcoinakkanode.wire._
import com.oohish.bitcoinscodec.messages.GetHeaders
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.messages.Headers
import akka.actor.ActorRef
import com.oohish.bitcoinscodec.structures.Message

object SPVNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVNode], networkParams)
}

class SPVNode(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams))
  val pm = context.actorOf(PeerManager.props(networkParams))

  def receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      pm ! PeerManager.UnicastMessage(GetAddr(), ref)
      sendBlockLocator(ref)
    case PeerManager.ReceivedMessage(msg, from) =>
      msgReceive(from)(msg)
  }

  def sendBlockLocator(ref: ActorRef) = {
    log.info("sending block locator")
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[GetBlockLocatorResponse]
      .map(blr =>
        PeerManager.UnicastMessage(
          GetHeaders(networkParams.PROTOCOL_VERSION, blr.bl), ref))
      .pipeTo(pm)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Headers(headers) =>
      headers.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
      if (!headers.isEmpty) sendBlockLocator(from)
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}