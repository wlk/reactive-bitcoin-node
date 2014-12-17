package com.github.yzernik.bitcoinakkanode.node

import scala.math.BigInt.int2bigInt

import com.oohish.bitcoinscodec.messages.Block
import com.oohish.bitcoinscodec.structures.BlockHeader
import com.oohish.bitcoinscodec.structures.Hash

import scodec.bits.HexStringSyntax

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
  def PROTOCOL_VERSION: Int = 70001
  def targetTimespan: Int
  def dnsSeeds: List[String]

  val TARGET_TIMESPAN: Int = 14 * 24 * 60 * 60; // 2 weeks per difficulty cycle, on average.
  val TARGET_SPACING: Int = 10 * 60; // 10 minutes per block.
  val INTERVAL: Int = TARGET_TIMESPAN / TARGET_SPACING;
}

object MainNetParams extends NetworkParameters {

  def addressHeader: Byte = 0
  def genesisBlock: Block = {
    val header: BlockHeader = BlockHeader(
      1L,
      Hash(hex"0000000000000000000000000000000000000000000000000000000000000000"),
      Hash(hex"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"),
      1231006505L,
      486604799L,
      2083236893L)
    Block(
      header,
      List()) // TODO: add tx's
  }
  def interval: Int = INTERVAL
  def packetMagic: Long = 0xD9B4BEF9L
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
  def genesisBlock: Block = {
    val header: BlockHeader = BlockHeader(
      1L,
      Hash(hex"0000000000000000000000000000000000000000000000000000000000000000"),
      Hash(hex"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"),
      1296688602L,
      0x1d00ffffL,
      414098458L)
    Block(
      header,
      List()) // TODO: add tx's
  }
  def interval: Int = INTERVAL
  def packetMagic: Long = 0x0709110B
  def port: Int = 18333
  def proofOfWorkLimit: BigInt = 0 //TODO
  def targetTimespan: Int = 0 //TODO
  def dnsSeeds: List[String] = List(
    "bitcoin.petertodd.org",
    "testnet-seed.bitcoin.petertodd.org",
    "testnet-seed.bluematt.me",
    "testnet-seed.alexykot.me",
    "stnet-seed.bitcoin.schildbach.de")

}

