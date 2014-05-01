package com.oohish.chain

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.oohish.peermessages.Block
import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Verack
import com.oohish.structures.uint32_t
import com.oohish.wire.BTCConnection.Outgoing
import com.oohish.wire.NetworkParameters

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.pipe

object SPVBlockChain {

  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVBlockChain], networkParams)

}

class SPVBlockChain(networkParams: NetworkParameters) extends Actor with ActorLogging {

  import context.dispatcher

  // initialize the block store.
  val store: BlockStore = new MemoryBlockStore()
  val blockChain = new BlockChain(store)
  val futureInitialized = blockChain.addBlock(networkParams.genesisBlock, true)

  def receive = listening()

  def listening(): Receive = {

    case Verack() => {
      log.info("FullBlockChain received Verack")
      val s = sender
      log.info("Becoming syncing")
      context.become(syncing(s))

      val futureBL = Chain.blockLocator(store)
      val futureGetHeaders = futureBL.map { bl =>
        Outgoing(
          GetHeaders(networkParams.PROTOCOL_VERSION, bl, Chain.emptyHashStop))
      }
      futureGetHeaders.pipeTo(sender)
    }

    case b: Block => {
      val futureAdded = blockChain.addBlock(b.toHeader)
    }

  }

  def syncing(peer: ActorRef): Receive = {

    case Headers(h) => {
      import scala.concurrent.duration._
      import scala.language.postfixOps

      if (sender == peer) {
        log.info("SPV blockchain received Headers with seq length: " + h.length)
        val futureAdded = blockChain.addBlocks(h)

        // ugly, but necessary.
        Await.result(futureAdded, 10 seconds)

        // if new headers received, ask for more.
        if (h.isEmpty) {
          log.info("Finished syncing. Becoming listening.")
          context.become(listening())

          val futureBL = for {
            bl <- Chain.blockLocator(store)
          } yield Outgoing(
            GetHeaders(networkParams.PROTOCOL_VERSION, bl, Chain.emptyHashStop))
          futureBL.pipeTo(sender)
        } else {
          log.info("Becoming syncing again-------------------------------------")
          context.become(syncing(peer))

          val futureBL = Chain.blockLocator(store)
          val futureGetHeaders = futureBL.map { bl =>
            Outgoing(
              GetHeaders(networkParams.PROTOCOL_VERSION, bl, Chain.emptyHashStop))
          }
          futureGetHeaders.pipeTo(sender)
        }
      }
    }

  }

}