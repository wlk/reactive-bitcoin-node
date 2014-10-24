package com.oohish.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.PeerManager

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

trait NetworkComponent {
  this: Actor with ActorLogging =>
  import context.dispatcher

  val pm: ActorRef
  implicit val timeout: Timeout

  def getConnectionCount(): Future[Int] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]
      .map(_.length)

  def getPeerInfo(): Future[List[InetSocketAddress]] =
    (pm ? PeerManager.GetPeers())
      .mapTo[List[InetSocketAddress]]

}