package com.oohish.structures

import java.nio.ByteOrder

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * IP
 */
object IP extends StructureReader[IP] {

  implicit override val byteOrder = ByteOrder.BIG_ENDIAN

  def decode(it: ByteIterator): IP = {

    val bytes: Array[Byte] = Array.fill(16)(0x0)

    it.getBytes(bytes)

    val lastBytes = bytes.slice(12, 16)

    IP(lastBytes.toList)
  }

  def apply(s: String): IP = {
    val nums = s.split("\\.").toList.map { num =>
      val i = num.toInt & 0xFF
      i.toByte
    }
    IP(nums)
  }

}

case class IP(ip: List[Byte]) extends Structure {

  implicit override val byteOrder = ByteOrder.BIG_ENDIAN

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    val bytesPrefix: List[Byte] =
      List.fill(10)(0x0.toByte) ++
        List.fill(2)(0xFF.toByte)

    val bytes: List[Byte] = bytesPrefix ++ ip.toList

    bb.putBytes(bytes.toArray)

    bb.result
  }

  override def toString = {
    val s = ip.map(_ & 0xFF).mkString(".")
    "IP(" + s + ")"
  }

}