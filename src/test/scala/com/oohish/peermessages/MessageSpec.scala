package com.oohish.peermessages

import scala.Array.canBuildFrom
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
import com.oohish.wire.MainNetParams
import com.oohish.wire.MessageTypeStage
import com.oohish.wire.Node
import com.oohish.wire.peermessagestage
import akka.io.PipelineContext
import akka.io.PipelineFactory
import akka.util.ByteString
import com.oohish.wire.BTCConnection
import com.oohish.structures.uint32_t

class peermessagespec extends FlatSpec with Matchers {

  val params = MainNetParams

  def version = Version(
    int32_t(60002),
    Node.services,
    int64_t(DateTime.parse("18/12/2012 10:12:33",
      DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")).getMillis() / 1000),
    NetworkAddress(Node.services, IP("0.0.0.0"), Port(0)),
    NetworkAddress(Node.services, IP("0.0.0.0"), Port(0)),
    uint64_t(BigInt(Array(0x3B, 0x2E, 0xB3, 0x5D, 0x8C, 0xE6, 0x17, 0x65).map(_.toByte).reverse)),
    VarStr("/Satoshi:0.7.2/"),
    int32_t(212672))

  val verack = new Verack()

  "A verack" should "have empty payload" in {

    val verackBytes = verack.encode

    verackBytes should be(ByteString())
    verackBytes.length should be(0)

  }

  it should "serialize to a bytestring with the right length" in {

    var bytes: ByteString = ByteString.empty

    val ctx = new PipelineContext {}

    val pipeline =
      PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(MainNetParams.packetMagic) >> new MessageTypeStage)(
        cmd => bytes = cmd.get,
        evt => {})

    pipeline.injectCommand(verack)

    bytes.length should be(24)

  }

  "A version" should "have the right payload size" in {

    val versionBytes = version.encode

    versionBytes.length should be(100)

  }

  it should "serialize to a bytestring with the right length" in {

    var bytes: ByteString = ByteString.empty

    val ctx = new PipelineContext {}

    val pipeline =
      PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(MainNetParams.packetMagic) >> new MessageTypeStage)(
        cmd => bytes = cmd.get,
        evt => {})

    pipeline.injectCommand(version)

    bytes.length should be(124)

  }

  it should "serialize to a bytestring with the right hexstring" in {

    var bytes: ByteString = ByteString.empty

    val ctx = new PipelineContext {}

    val pipeline =
      PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(MainNetParams.packetMagic) >> new MessageTypeStage)(
        cmd => bytes = cmd.get,
        evt => {})

    pipeline.injectCommand(version)

    val byteArray = bytes.compact.toParArray.toArray

    val hexString = HexBytesUtil.bytes2hex(byteArray)

    val expected = "F9 BE B4 D9 76 65 72 73 69 6F 6E 00 00 00 00 00 64 00 00 00 3B 64 8D 5A 62 EA 00 00 01 00 00 00 00 00 00 00 11 B2 D0 50 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 00 00 00 00 00 00 3B 2E B3 5D 8C E6 17 65 0F 2F 53 61 74 6F 73 68 69 3A 30 2E 37 2E 32 2F C0 3E 03 00".replace(" ", "").toLowerCase()

    hexString should be(expected)

  }

  it should "deserialize back to its original value" in {

    val version = Version(
      BTCConnection.versionNum(params),
      Node.services,
      int64_t(DateTime.parse("18/12/2012 10:12:33",
        DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")).getMillis() / 1000),
      NetworkAddress(Node.services, IP("106.69.141.207"), Port(8333)),
      NetworkAddress(Node.services, IP("127.0.0.1"), Port(8333)),
      BTCConnection.genNonce,
      VarStr("/Satoshi:0.7.2/"),
      int32_t(1))

    var bytes: ByteString = ByteString.empty
    var finalVersion: MessagePayload = Verack()

    val ctx = new PipelineContext {}

    val pipeline =
      PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(MainNetParams.packetMagic) >> new MessageTypeStage)(
        cmd => bytes = cmd.get,
        evt => finalVersion = evt.get)

    pipeline.injectCommand(version)

    pipeline.injectEvent(bytes)

    finalVersion should be(version)

  }

  "A version ByteString" should "deserialize to a Version object" in {

    val bytes = ByteString(-7, -66, -76, -39, 118, 101, 114, 115, 105, 111, 110, 0, 0, 0, 0, 0, 100, 0, 0, 0, -46, 123, 119, -83, 113, 17, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, -53, 105, 62, 83, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 75, 25, -118, -71, -39, 71, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 72, -72, -117, -71, 32, -115, 33, 39, 61, 44, -46, 9, 3, 68, 15, 47, 83, 97, 116, 111, 115, 104, 105, 58, 48, 46, 56, 46, 54, 47, -23, 124, 4, 0)

    var msg: MessagePayload = Verack()

    val ctx = new PipelineContext {}

    val pipeline =
      PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(MainNetParams.packetMagic) >> new MessageTypeStage)(
        cmd => {},
        evt => msg = evt.get)

    pipeline.injectEvent(bytes)

    msg.isInstanceOf[Version] should be(true)

  }

}