package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.pipe

object Node {
  def props(networkParameters: NetworkParameters) =
    Props(classOf[Node], networkParameters)

  sealed trait APICommand
  case class GetBestBlockHash() extends APICommand
  case class GetBlock(hash: Hash) extends APICommand
  case class GetBlockCount() extends APICommand
  case class GetBlockHash(index: Int) extends APICommand
  case class GetConnectionCount() extends APICommand
  case class GetPeerInfo() extends APICommand
}

class Node(networkParameters: NetworkParameters) extends Actor with ActorLogging {
  import context.dispatcher
  import Node._

  val blockchain: ActorRef = ???
  val networkController: ActorRef = ???

  def receive: Receive = {
    case GetConnectionCount() =>
      getConnectionCount().pipeTo(sender)
    case GetPeerInfo() =>
      getPeerInfo().pipeTo(sender)
    case GetBestBlockHash() =>
      getBestBlockHash.pipeTo(sender)
    case GetBlockCount() =>
      getBlockCount().pipeTo(sender)
    case GetBlockHash(index) =>
      getBlockHash(index).pipeTo(sender)
    case cmd: APICommand =>
      sender ! "Command not found."
  }

  private def getConnectionCount(): Future[Int] = ???

  private def getPeerInfo(): Future[List[NetworkAddress]] = ???

  private def getBestBlockHash(): Future[Hash] = ???

  private def getBlockCount(): Future[Int] = ???

  private def getBlockHash(index: Int): Future[Hash] = ???

}