package com.oohish.chain

import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Verack
import com.oohish.structures.BlockHeader
import com.oohish.structures.VarStruct
import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import com.oohish.wire.BTCConnection
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.actorRef2Scala
import com.oohish.wire.NetworkParameters

object SPVBlockChain {

  case class AddBlocks(newblocks: List[BlockHeader])
  case class NumBlocks(n: Int)

  def props(networkParams: NetworkParameters) =
    Props(classOf[SPVBlockChain], networkParams)

}

class SPVBlockChain(networkParams: NetworkParameters) extends Actor with ActorLogging {
  import SPVBlockChain._
  import BTCConnection._

  //vector representing the blockchain.
  var chain: Vector[BlockHeader] = Vector(BlockHeader.fromBlock(networkParams.genesisBlock))

  def blockLocator(): VarStruct[char32] = {
    val indices = Chain.blockLocatorIndices(chain.length)
    val hashList = indices.map { index =>
      Chain.blockHash(chain(index))
    }
    VarStruct(hashList)
  }

  def receive = {

    case Verack() => {
      log.info("SPV store received Verack")
      sender ! Outgoing(
        GetHeaders(uint32_t(60002), blockLocator, Chain.emptyHashStop))
    }

    case Headers(h) => {
      val newHeaders = h.seq
      log.debug("SPV store received Headers with seq length: " + newHeaders.length)

      var newChain = chain

      val prev = newHeaders.headOption.flatMap { newHeader =>
        newChain.find { header =>
          newHeader.prev_block == Chain.blockHash(header)
        }
      }

      for (p <- prev) {
        val i = newChain.indexOf(p)
        newChain = newChain.slice(0, i + 1)

        for (newHeader <- newHeaders) {
          if (newHeader.prev_block == Chain.blockHash(newChain.last)) {
            newChain = newChain :+ newHeader
            log.debug("added header at index: " + (newChain.size - 1))
          }
        }

        if (newChain.size > chain.size) {
          chain = newChain
          log.info("number of headers: " + chain.size + ", last existing block hash: " + Chain.blockHash(chain.last))
          sender ! Outgoing(
            GetHeaders(uint32_t(60002), blockLocator, Chain.emptyHashStop))
        } else {
          log.info("newChain failed to replace chain.")
        }
      }
    }

    case other => {
      log.debug("SPV store received other: " + other)
    }
  }

}