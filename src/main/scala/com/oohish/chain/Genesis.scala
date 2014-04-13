package com.oohish.chain

import scala.Array.canBuildFrom

import com.oohish.structures.BlockHeader
import com.oohish.structures.VarInt
import com.oohish.structures.char32
import com.oohish.structures.uint32_t
import com.oohish.util.HexBytesUtil

object Genesis {

  val header = {
    BlockHeader(
      uint32_t(1), //version
      char32(HexBytesUtil.hex2bytes("0000000000000000000000000000000000000000000000000000000000000000").toList), //prev block
      char32(HexBytesUtil.hex2bytes("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b").toList), //merkle root
      uint32_t(1231006505), //timestamp
      uint32_t(486604799), //bits
      uint32_t(2083236893), //nonce
      VarInt(1) //number of transactions
      )
  }

}