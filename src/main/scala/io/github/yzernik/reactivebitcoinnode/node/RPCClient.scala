package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.github.yzernik.btcio.actors.BTC.PeerInfo

class RPCClient(node: ActorRef) {

  implicit val t = Timeout(10 seconds)

  /**
   * Get the peer info of the connected peers.
   */
  def getPeerInfo: Future[List[PeerInfo]] =
    queryNode(Node.GetPeerInfo).mapTo[List[PeerInfo]]

  /**
   * Get the count of the connected peers.
   */
  def getConnectionCount: Future[Int] =
    queryNode(Node.GetConnectionCount).mapTo[Int]

  /**
   * Get the count of blockchain blocks.
   */
  def getBlockCount: Future[Int] =
    queryNode(Node.GetBlockCount).mapTo[Int]

  /**
   * Ask the Bitcoin node for the result of the query.
   */
  private def queryNode(cmd: Node.APICommand) =
    (node ? cmd)

}