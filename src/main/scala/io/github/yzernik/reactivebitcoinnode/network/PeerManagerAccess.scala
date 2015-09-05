package io.github.yzernik.reactivebitcoinnode.network

import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

/**
 * @author yzernik
 */
class PeerManagerAccess(peerManager: ActorRef) extends NetworkAccess {

  implicit val timeout = Timeout(5 seconds)

  def getConnections(implicit executor: scala.concurrent.ExecutionContext) =
    (peerManager ? PeerManager.GetConnections).mapTo[Set[ActorRef]]

  def addNode(socketAddr: InetSocketAddress, connect: Boolean) =
    peerManager ! PeerManager.AddNode(socketAddr, connect)

}