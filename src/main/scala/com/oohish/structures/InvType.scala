package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * InvType
 */
object InvType extends StructureReader[InvType] {

  def decode(it: ByteIterator): InvType = {
    val n = uint32_t.decode(it).n.toInt
    InvType(typeName(n))
  }

  def typeName(n: Int): String = {
    n match {
      case 0 => "ERROR"
      case 1 => "MSG_TX"
      case 2 => "MSG_BLOCK"
    }
  }
}

case class InvType(name: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    val n = typeInt(name)
    bb ++= uint32_t(n).encode
    bb.result
  }

  def typeInt(s: String): Int = {
    s match {
      case "ERROR" => 0
      case "MSG_TX" => 1
      case "MSG_BLOCK" => 2
    }
  }

}