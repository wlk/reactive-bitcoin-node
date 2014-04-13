package com.oohish.structures

import java.nio.ByteOrder

import akka.util.ByteIterator
import akka.util.ByteString

trait StructureReader[T <: Structure] {

  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN

  def decode(it: ByteIterator): T

}

abstract class Structure {

  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN

  def encode: ByteString

}