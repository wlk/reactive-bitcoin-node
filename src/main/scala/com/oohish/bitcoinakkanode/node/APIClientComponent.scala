package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

trait APIClientComponent {
  this: Actor =>
  import context.dispatcher

  def apiClient: ActorRef

}