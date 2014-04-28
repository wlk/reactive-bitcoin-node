package com.oohish.peermessages

import scala.collection.parallel.traversable2ops

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.oohish.structures.IP
import com.oohish.structures.NetworkAddress
import com.oohish.structures.Port
import com.oohish.structures.VarStr
import com.oohish.structures.int32_t
import com.oohish.structures.int64_t
import com.oohish.structures.uint64_t
import com.oohish.util.HexBytesUtil
import com.oohish.wire.BTCConnection
import com.oohish.wire.Node

class DataTypesSpec extends FlatSpec with Matchers {

  "A version number" should "have the right size" in {

    val versionNumber = int32_t(60002)
    val bytes = versionNumber.encode

    bytes.length should be(4)

  }

  it should "have the right hex string" in {

    val versionNumber = int32_t(60002)
    val bytes = versionNumber.encode

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "62 EA 00 00".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

  "A services number" should "have the right size" in {

    val services = Node.services
    val bytes = services.encode

    bytes.length should be(8)

  }

  it should "have the right hex string" in {

    val services = Node.services
    val bytes = services.encode

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "01 00 00 00 00 00 00 00".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

  "A timestamp" should "have the right size" in {

    val time = int64_t(DateTime.now().getMillis() / 1000)
    val bytes = time.encode

    bytes.length should be(8)

  }

  it should "have the right hex string" in {

    val time = int64_t(DateTime.parse("18/12/2012 10:12:33",
      DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")).getMillis() / 1000)
    val bytes = time.encode

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "11 B2 D0 50 00 00 00 00".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

  "An addr" should "have the right size" in {

    val addr = NetworkAddress(
      uint64_t(BigInt(1)),
      IP("10.0.0.1"),
      Port(8333))
    val bytes = addr.encode

    bytes.length should be(26)

  }

  it should "have the right hex string" in {

    val addr = NetworkAddress(Node.services, IP("10.0.0.1"), Port(8333))
    val bytes = addr.encode

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 0A 00 00 01 20 8D".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

  "A nonce" should "have the right size" in {

    val nonce = Node.genNonce
    val bytes = nonce.encode

    bytes.length should be(8)

  }

  it should "deserialize back to itself" in {

    val nonce = Node.genNonce
    val bytes = nonce.encode

    val it = bytes.iterator
    val finalNonce = uint64_t.decode(it)

    finalNonce should be(nonce)

  }

  "A user-agent" should "have the right hex string" in {

    val userAgent = VarStr("/Satoshi:0.7.2/")
    val bytes = userAgent.encode

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "0F 2F 53 61 74 6F 73 68 69 3A 30 2E 37 2E 32 2F".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

}