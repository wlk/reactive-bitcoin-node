package io.github.yzernik.reactivebitcoinnode.node

import scala.math.BigInt.int2bigInt

import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.BlockHeader
import io.github.yzernik.bitcoinscodec.structures.Hash
import scodec.bits.HexStringSyntax

/*
 * Taken from bitcoinj's NetworkParameters
 */

trait NetworkParameters {
  def addressHeader: Byte
  def genesisBlock: Block
  def interval: Int = NetworkParameters.INTERVAL
  def packetMagic: Long
  def port: Int
  def proofOfWorkLimit: BigInt = 0
  def PROTOCOL_VERSION: Int = 70001
  def targetTimespan: Int = NetworkParameters.TARGET_TIMESPAN
  def dnsSeeds: List[String]
}

object NetworkParameters {
  val TARGET_TIMESPAN: Int = 14 * 24 * 60 * 60; // 2 weeks per difficulty cycle, on average.
  val TARGET_SPACING: Int = 10 * 60; // 10 minutes per block.
  val INTERVAL: Int = TARGET_TIMESPAN / TARGET_SPACING;
}

object MainNetParams extends NetworkParameters {
  def addressHeader: Byte = 0
  def genesisBlock: Block =
    Block(
      BlockHeader(1L,
        Hash(hex"0000000000000000000000000000000000000000000000000000000000000000"),
        Hash(hex"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"),
        1231006505L,
        486604799L,
        2083236893L),
      List()) // TODO: add tx's
  def packetMagic: Long = 0xD9B4BEF9L
  def port: Int = 8333
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
      BlockHeader(1L,
        Hash(hex"0000000000000000000000000000000000000000000000000000000000000000"),
        Hash(hex"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"),
        1296688602L,
        0x1d00ffffL,
        414098458L),
      List()) // TODO: add tx's
  def packetMagic: Long = 0x0709110B
  def port: Int = 18333
  def dnsSeeds: List[String] = List(
    "bitcoin.petertodd.org",
    "testnet-seed.bitcoin.petertodd.org",
    "testnet-seed.bluematt.me",
    "testnet-seed.alexykot.me",
    "stnet-seed.bitcoin.schildbach.de")
}

