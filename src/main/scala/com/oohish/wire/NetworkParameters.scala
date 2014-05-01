package com.oohish.wire

import scala.math.BigInt.int2bigInt

import com.oohish.peermessages.Block
import com.oohish.peermessages.Tx
import com.oohish.structures.OutPoint
import com.oohish.structures.TxIn
import com.oohish.structures.TxOut

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
  def PROTOCOL_VERSION: Long = 70001
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
      1, //version
      "0000000000000000000000000000000000000000000000000000000000000000", //prev block
      "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", //merkle root
      1231006505, //timestamp
      486604799, //bits
      2083236893, //nonce
      List(
        Tx(
          1, //version
          List(
            TxIn(
              OutPoint(
                "0000000000000000000000000000000000000000000000000000000000000000", //prev block
                4294967295L //n
                ),
              "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73", // txins
              Int.MaxValue.toLong * 2)),
          List(
            TxOut(
              5000000000L,
              "04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f OP_CHECKSIG")), // txins
          0 //lockTime
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
      1, //version
      "0000000000000000000000000000000000000000000000000000000000000000", //prev block
      "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", //merkle root
      1296688602, //timestamp
      486604799, //bits
      414098458, //nonce
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