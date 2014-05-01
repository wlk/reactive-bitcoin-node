package com.oohish.wire

import akka.util.ByteString
import com.oohish.peermessages.MessagePayload
import java.security.MessageDigest
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Message {

  val commands = List(
    "verack",
    "version",
    "addr",
    "inv",
    "getdata",
    "notfound",
    "getblocks",
    "getheaders",
    "tx",
    "block",
    "headers",
    "getaddr",
    "mempool",
    "ping",
    "pong",
    "alert")

  def commandString(command: String) = {
    command + new String(Array.fill(12 - command.length())(0x0.toChar))
  }

  def commandFromCommandString(commandString: String): String = {
    commands.filter { str =>
      commandString.startsWith(str)
    }.headOption.getOrElse("")
  }

  /*
       * Calculates the checksum of the payload.
       */
  def checksum(data: ByteString): Long = {
    val payloadBytes = Array.fill(data.length)(0x0.toByte)
    data.iterator.getBytes(payloadBytes)
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hash1 = messageDigest.digest(payloadBytes)
    val hash2 = messageDigest.digest(hash1)
    val padding: Array[Byte] = Array.fill(4)(0)
    val byteBuffer = ByteBuffer.wrap(hash2.slice(0, 4) ++ padding).order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.getLong()
  }

}

abstract class Message {
  def magic: Long
  def command: String
}

/**
 * @see
 */
case class FullMessage(magic: Long, command: String, payload: MessagePayload)
  extends Message

case class PartialMessage(magic: Long, command: String, body: ByteString)
  extends Message {

}