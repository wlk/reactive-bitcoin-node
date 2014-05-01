package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString
import com.oohish.util.HexBytesUtil

object Script extends StructureReader[Script] {

  def decode(it: ByteIterator): Script = {
    val n = VarInt.decode(it).n.toInt

    val chArr: Array[Byte] = Array.fill(n)(0x0)
    it.getBytes(chArr)

    //val s = new String(chArr)

    val s = HexBytesUtil.bytes2hex(chArr)

    Script(s)
  }

}

case class Script(s: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    bb ++= VarInt(s.length()).encode
    //    val charArr = s.toCharArray().map(_.toByte)
    val chArr = HexBytesUtil.hex2bytes(s)
    bb.putBytes(chArr)

    bb.result
  }

}