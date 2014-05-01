package com.oohish.chain

import scala.collection.parallel.traversable2ops

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.oohish.peermessages.Block
import com.oohish.util.HexBytesUtil
import com.oohish.wire.MainNetParams

class BlockSpec extends FlatSpec with Matchers {

  "A block header" should "decode back to itself" in {

    val bh = {
      Block(
        1, //version
        "6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000", //prev block
        "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", //merkle root
        1231006505, //timestamp
        486604799, //bits
        2083236893, //nonce
        List() //number of transactions
        )
    }

    val bytes = bh.encode

    val it = bytes.iterator

    val finalBh = Block.decode(it)

    finalBh should be(bh)

  }

  "Genesis block" should "encode to the right hex string" in {

    val expectedGenesisEncoding =
      """   01 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   
   00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00   
   00 00 00 00 3B A3 ED FD  7A 7B 12 B2 7A C7 2C 3E   
   67 76 8F 61 7F C8 1B C3  88 8A 51 32 3A 9F B8 AA   
   4B 1E 5E 4A 29 AB 5F 49  FF FF 00 1D 1D AC 2B 7C   
   00""".replaceAll("[^0-9A-Fa-f]", "").toLowerCase

    val genesisBytes = MainNetParams.genesisBlock.toHeader.encode.compact.toParArray.toArray

    val genesisEncoding = HexBytesUtil.bytes2hex(genesisBytes)

    genesisEncoding should be(expectedGenesisEncoding)

  }

  it should "decode back to itself" in {

    val genesisBytes = MainNetParams.genesisBlock.toHeader.encode

    val it = genesisBytes.iterator

    val finalGenesisBlockHeader = Block.decode(it)

    finalGenesisBlockHeader should be(MainNetParams.genesisBlock.toHeader)

  }

  it should "have the right hash" in {

    val expectedGenesisHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"

    val genesis = MainNetParams.genesisBlock.toHeader

    val genesisHash = genesis.hash

    genesisHash should be(expectedGenesisHash)

  }

}