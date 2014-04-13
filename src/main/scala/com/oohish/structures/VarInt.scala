package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

object VarInt extends StructureReader[VarInt] {

  def decode(it: ByteIterator): VarInt = {

    val first = it.getByte & 0xFF

    val n: Long =
      first match {
        case 0xff => {
          uint64_t.decode(it).n.toLong
        }
        case 0xfe => {
          uint32_t.decode(it).n
        }
        case 0xfd => {
          uint16_t.decode(it).n
        }
        case _ => {
          first
        }
      }

    VarInt(n)
  }

}

case class VarInt(n: Long) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder

    if (n < 0xfd) {
      bb.putByte(n.toByte)
    } else if (n <= 0xffff) {
      bb.putByte(0xfd.toByte)
      bb ++= uint16_t(n).encode
    } else if (n <= 0xffffffff) {
      bb.putByte(0xfe.toByte)
      bb ++= uint32_t(n).encode
    }

    bb.result
  }

}