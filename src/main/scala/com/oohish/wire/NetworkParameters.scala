package com.oohish.wire

import scala.math.BigInt.int2bigInt
import com.oohish.peermessages.Block
import com.oohish.structures.VarStruct
import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import com.oohish.util.HexBytesUtil
import com.oohish.peermessages.Tx
import com.oohish.structures.TxIn
import com.oohish.structures.OutPoint
import com.oohish.structures.TxOut
import com.oohish.structures.int64_t

/*
 * Taken from bitcoinj's NetworkParameters
 */

trait NetworkParameters {
  def addressHeader: Byte
  def genesisBlock: Block
  def interval: Int
  def packetMagic: Long
  def port: Int
  def proofOfWorkLimit: BigInt
  def PROTOCOL_VERSION: uint32_t = uint32_t(70001)
  def targetTimespan: Int
  def dnsSeeds: List[String]

  val TARGET_TIMESPAN: Int = 14 * 24 * 60 * 60; // 2 weeks per difficulty cycle, on average.
  val TARGET_SPACING: Int = 10 * 60; // 10 minutes per block.
  val INTERVAL: Int = TARGET_TIMESPAN / TARGET_SPACING;
}

object MainNetParams extends NetworkParameters {

  def addressHeader: Byte = 0
  def genesisBlock: Block =
    Block(
      uint32_t(1), //version
      char32("0000000000000000000000000000000000000000000000000000000000000000"), //prev block
      char32("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"), //merkle root
      uint32_t(1231006505), //timestamp
      uint32_t(486604799), //bits
      uint32_t(2083236893), //nonce
      List(
        Tx(
          uint32_t(1), //version
          List(
            TxIn(
              OutPoint(
                char32("0000000000000000000000000000000000000000000000000000000000000000"), //prev block
                uint32_t(4294967295L) //n
                ),
              "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73", // txins
              uint32_t(Int.MaxValue.toLong * 2))),
          List(
            TxOut(
              int64_t(5000000000L),
              "04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f OP_CHECKSIG")), // txins
          uint32_t(0) //lockTime
          )))
  def interval: Int = INTERVAL
  def packetMagic: Long = 0xD9B4BEF9
  def port: Int = 8333
  def proofOfWorkLimit: BigInt = 0 //TODO
  def targetTimespan: Int = 0 //TODO
  def dnsSeeds: List[String] = List(
    "seed.bitcoin.sipa.be",
    "dnsseed.bluematt.me",
    "dnsseed.bitcoin.dashjr.org",
    "bitseed.xf2.org")

}

object TestNet3Params extends NetworkParameters {

  def addressHeader: Byte = 0
  def genesisBlock: Block =
    Block(
      uint32_t(1), //version
      char32("0000000000000000000000000000000000000000000000000000000000000000"), //prev block
      char32("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"), //merkle root
      uint32_t(1296688602), //timestamp
      uint32_t(486604799), //bits
      uint32_t(414098458), //nonce
      List())
  def interval: Int = INTERVAL
  def packetMagic: Long = 0x0709110B
  def port: Int = 18333
  def proofOfWorkLimit: BigInt = 0 //TODO
  def targetTimespan: Int = 0 //TODO
  def dnsSeeds: List[String] = List(
    "bitcoin.petertodd.org",
    "testnet-seed.bitcoin.petertodd.org")

}