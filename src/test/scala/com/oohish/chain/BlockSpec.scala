package com.oohish.chain

import collection.mutable.Stack
import org.scalatest._
import akka.util.ByteStringBuilder
import com.oohish.util.HexBytesUtil
import com.oohish.structures.BlockHeader
import com.oohish.structures.uint32_t
import com.oohish.structures.char32
import com.oohish.structures.VarInt
import com.oohish.wire.MainNetParams

class BlockSpec extends FlatSpec with Matchers {

  "A block header" should "decode back to itself" in {

    val bh = {
      BlockHeader(
        uint32_t(1), //version
        char32(HexBytesUtil.hex2bytes("6fe28c0ab6f1b372c1a6a246ae63f74f931e8365e15a089c68d6190000000000").toList), //prev block
        char32(HexBytesUtil.hex2bytes("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b").toList), //merkle root
        uint32_t(1231006505), //timestamp
        uint32_t(486604799), //bits
        uint32_t(2083236893), //nonce
        VarInt(1) //number of transactions
        )
    }

    val bytes = bh.encode

    val it = bytes.iterator

    val finalBh = BlockHeader.decode(it)

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

    val genesisBytes = BlockHeader.fromBlock(MainNetParams.genesisBlock).encode.compact.toParArray.toArray

    val genesisEncoding = HexBytesUtil.bytes2hex(genesisBytes)

    genesisEncoding should be(expectedGenesisEncoding)

  }

  it should "decode back to itself" in {

    val genesisBytes = BlockHeader.fromBlock(MainNetParams.genesisBlock).encode

    val it = genesisBytes.iterator

    val finalGenesisBlockHeader = BlockHeader.decode(it)

    finalGenesisBlockHeader should be(BlockHeader.fromBlock(MainNetParams.genesisBlock))

  }

  it should "have the right hash" in {

    val expectedGenesisHashString = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"

    val genesis = BlockHeader.fromBlock(MainNetParams.genesisBlock)

    val genesisHash = Chain.blockHash(genesis)

    val genesisHashString = HexBytesUtil.bytes2hex(genesisHash.bytes.toArray)

    genesisHashString should be(expectedGenesisHashString)

  }

}