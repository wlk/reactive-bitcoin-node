package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

object VarStr extends StructureReader[VarStr] {

  def decode(it: ByteIterator): VarStr = {
    val n = VarInt.decode(it).n.toInt

    val chArr: Array[Byte] = Array.fill(n)(0x0)
    it.getBytes(chArr)

    val s = new String(chArr)

    VarStr(s)
  }

}

case class VarStr(s: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    bb ++= VarInt(s.length()).encode
    val charArr = s.toCharArray().map(_.toByte)
    bb.putBytes(charArr)

    bb.result
  }

}