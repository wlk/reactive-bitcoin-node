package com.oohish.structures

import akka.util.ByteIterator
import akka.util.ByteString

/*
 * InvVect
 */
object InvVect extends StructureReader[InvVect] {

  def decode(it: ByteIterator): InvVect = {
    InvVect(
      InvType.decode(it),
      char32.decode(it).s)
  }
}

case class InvVect(t: InvType, hash: String) extends Structure {

  def encode: ByteString = {
    val bb = ByteString.newBuilder
    bb ++= t.encode
    bb ++= char32(hash).encode
    bb.result
  }

}