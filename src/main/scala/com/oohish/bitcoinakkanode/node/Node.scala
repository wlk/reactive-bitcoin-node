package com.oohish.bitcoinakkanode.node

import scala.language.postfixOps

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.PeerManager
import com.oohish.bitcoinscodec.messages.GetAddr
import com.oohish.bitcoinscodec.structures.Message

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

trait Node extends Actor with ActorLogging {

  def networkParams: NetworkParameters

  def blockchain: ActorRef
  val pm = context.actorOf(PeerManager.props(networkParams))

  def receive = {
    case PeerManager.PeerConnected(ref, addr) =>
      pm ! PeerManager.UnicastMessage(GetAddr(), ref)
      blockDownload(ref)
    case PeerManager.ReceivedMessage(msg, from) =>
      msgReceive(from)(msg)
  }

  def blockDownload(ref: ActorRef): Unit

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit]

}