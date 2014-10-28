package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.language.postfixOps

import org.joda.time.DateTime

import com.oohish.bitcoinakkanode.node.APIClient.APICommand
import com.oohish.bitcoinakkanode.util.Util
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object Node {
  val userAgent: String = "/bitcoin-akka-node:0.1.0/"

  case class GetVersion(remote: InetSocketAddress, local: InetSocketAddress)
  case class SyncPeer(ref: ActorRef, v: Version)
}

trait Node extends Actor with NetworkParamsComponent {
  this: APIClientComponent =>
  import context.dispatcher

  def services: BigInt
  def relay: Boolean

  def receive: Receive = {
    case cmd: APICommand =>
      apiClient ! cmd
    case msg: Message =>
  }

}