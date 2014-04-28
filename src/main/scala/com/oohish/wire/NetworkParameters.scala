package com.oohish.wire

import scala.math.BigInt.int2bigInt

import com.oohish.peermessages.Block
import com.oohish.structures.VarStruct
import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import com.oohish.util.HexBytesUtil

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
  def PROTOCOL_VERSION: Int
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
      List())
  def interval: Int = INTERVAL
  def packetMagic: Long = 0xD9B4BEF9
  def port: Int = 8333
  def proofOfWorkLimit: BigInt = 0 //TODO
  def PROTOCOL_VERSION: Int = 0 //TODO
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
  def PROTOCOL_VERSION: Int = 0 //TODO
  def targetTimespan: Int = 0 //TODO
  def dnsSeeds: List[String] = List(
    "bitcoin.petertodd.org",
    "testnet-seed.bitcoin.petertodd.org")

}