package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import io.github.yzernik.bitcoinscodec.messages.Headers

object BlockDownloader {
  def props() =
    Props(classOf[BlockDownloader])
}

class BlockDownloader extends Actor {

  context.setReceiveTimeout(10 seconds)

  var preferredPeers = Set.empty[ActorRef]

  def receive: Receive = {
    case Headers(blocks) =>
      // To set in a response to a message
      context.setReceiveTimeout(100 milliseconds)
      
    case ReceiveTimeout =>
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      throw new RuntimeException("Receive timed out")
  }

}