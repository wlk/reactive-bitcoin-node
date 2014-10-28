package com.oohish.bitcoinakkanode.node

import scala.concurrent.Future
import scala.language.postfixOps
import com.oohish.bitcoinscodec.structures.Hash
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.oohish.bitcoinakkanode.blockchain.BlockChain
import com.oohish.bitcoinscodec.messages.Block

trait BlockChainComponent {
  this: Actor =>
  import context.dispatcher

  def blockChain: ActorRef

}