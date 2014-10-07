package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.NetworkParameters
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.Hash
import com.oohish.bitcoinakkanode.util.Util

object BlockChain {
  def props(networkParams: NetworkParameters) =
    Props(classOf[BlockChain], networkParams)

  case class StoredBlock(b: Block, height: Long)
  case class GetHeight()
  case class GetHeightResponse(height: Long)

}

trait BlockChain extends Actor with ActorLogging {
  import com.oohish.bitcoinakkanode.node.BlockChain._
  import scodec.bits.BitVector
  import com.oohish.bitcoinscodec.structures.BlockHeader

  val genesis: Block
  val g = StoredBlock(genesis, 1)
  var blocks: Map[Hash, StoredBlock] = Map.empty
  var chainHead = StoredBlock(genesis, 1)

  def receive = {
    case GetHeight() =>
      sender ! GetHeightResponse(chainHead.height)
  }

  def isValidBlock(b: Block): Boolean

  def blockHash(b: Block): Hash = {
    val bytes = BlockHeader.codec.encode(b.block_header)
      .getOrElse(BitVector.empty).toByteArray
    Util.blockHash(bytes)
  }
}