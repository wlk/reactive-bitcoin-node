package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.spv.SPVBlockChain

import akka.actor.Actor

trait SPVBlockChainComponent extends BlockChainComponent with NetworkParamsComponent {
  this: Actor =>

  val blockchain = context.actorOf(SPVBlockChain.props(networkParams), "spv-blockchain")

}