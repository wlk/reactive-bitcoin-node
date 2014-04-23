package com.oohish.structures

import com.oohish.peermessages.Block

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * BlockHeader
 */
object BlockHeader extends StructureReader[BlockHeader] {

  def decode(it: ByteIterator): BlockHeader = {
    BlockHeader(
      uint32_t.decode(it),
      char32.decode(it),
      char32.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it),
      uint32_t.decode(it),
      VarInt.decode(it))
  }

  def fromBlock(block: Block): BlockHeader = {
    BlockHeader(
      block.version,
      block.prev_block,
      block.merkle_root,
      block.timestamp,
      block.bits,
      block.nonce,
      VarInt(0))
  }
}

case class BlockHeader(
  version: uint32_t,
  prev_block: char32,
  merkle_root: char32,
  timestamp: uint32_t,
  bits: uint32_t,
  nonce: uint32_t,
  txn_count: VarInt) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= version.encode
    bb ++= prev_block.encode
    bb ++= merkle_root.encode
    bb ++= timestamp.encode
    bb ++= bits.encode
    bb ++= nonce.encode
    bb ++= txn_count.encode
    bb.result
  }

}