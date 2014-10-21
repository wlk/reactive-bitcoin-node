package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object ListenerNode {
  def props(networkParams: NetworkParameters) =
    Props(classOf[ListenerNode], networkParams)
}

class ListenerNode(np: NetworkParameters) extends Node {

  def networkParams = np

  def receive = ready

  def ready: Receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      pm ! PeerManager.UnicastMessage(GetAddr(), ref)
    case PeerManager.ReceivedMessage(msg, from) =>
      msgReceive(from)(msg)
    case cmd: APICommand =>
      receiveNetworkCommand(cmd)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case _ =>
  }

}