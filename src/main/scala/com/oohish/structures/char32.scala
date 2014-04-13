package com.oohish.structures

import akka.util.ByteString
import akka.util.ByteIterator
import com.oohish.util.HexBytesUtil

/*
 * char32
 */
object char32 extends StructureReader[char32] {

  def decode(it: ByteIterator): char32 = {

    val bytesArr: Array[Byte] = Array.fill(32)(0x0)
    it.getBytes(bytesArr)
    val bytes = bytesArr.toList.reverse

    char32(bytes)
  }
}

case class char32(bytes: List[Byte]) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    val bytesArr = bytes.reverse.toArray
    bb.putBytes(bytesArr)

    bb.result
  }

  override def toString = "char32(" + HexBytesUtil.bytes2hex(bytes.toArray) + ")"

}