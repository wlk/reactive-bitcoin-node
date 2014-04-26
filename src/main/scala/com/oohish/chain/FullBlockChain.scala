package com.oohish.chain

import com.oohish.wire.NetworkParameters
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.oohish.peermessages.Verack
import com.oohish.wire.BTCConnection.Outgoing
import com.oohish.peermessages.GetHeaders
import com.oohish.structures.uint32_t

object FullBlockChain {

  def props(networkParams: NetworkParameters) =
    Props(classOf[FullBlockChain], networkParams)

}

class FullBlockChain(networkParams: NetworkParameters) extends Actor with ActorLogging {

  val store: BlockStore = new MemoryBlockStore()
  val genesis = networkParams.genesisBlock.toHeader
  store.put(genesis)
  store.setChainHead(genesis)

  def receive = {

    case Verack() => {
      log.info("SPV store received Verack")
      // sender ! Outgoing(
      //   GetHeaders(uint32_t(60002), blockLocator, Chain.emptyHashStop))
    }

  }

}