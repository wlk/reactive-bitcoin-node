package com.oohish.bitcoinakkanode.spv

import com.oohish.bitcoinakkanode.node.BlockChainComponent
import com.oohish.bitcoinakkanode.node.NetworkParamsComponent

import akka.actor.Actor

trait SPVBlockChainComponent extends BlockChainComponent {
  this: Actor with NetworkParamsComponent =>

  val blockChain = context.actorOf(SPVBlockChain.props(networkParams), "spv-blockchain")

}