package com.oohish.structures

import scala.Array.canBuildFrom
import akka.util.ByteIterator
import akka.util.ByteString
import com.oohish.util.HexBytesUtil

/*
 * char32
 */
object char32 extends StructureReader[char32] {

  def decode(it: ByteIterator): char32 = {

    val bytesArr: Array[Byte] = Array.fill(32)(0x0)
    it.getBytes(bytesArr)
    val s = HexBytesUtil.bytes2hex(bytesArr.reverse)

    char32(s)
  }
}

case class char32(s: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    val bytesArr = HexBytesUtil.hex2bytes(s).reverse
    bb.putBytes(bytesArr)

    bb.result
  }

}