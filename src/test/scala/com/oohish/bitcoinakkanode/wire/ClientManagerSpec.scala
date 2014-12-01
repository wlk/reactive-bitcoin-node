package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.math.BigInt.int2bigInt
import scala.math.BigInt.long2bigInt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.oohish.bitcoinakkanode.wire.Handshaker.FinishedHandshake
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress
import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe

/**
 * a Test for ClientManager
 */
class ClientManagerSpec
  extends TestKit(ActorSystem("HandshakerSpec",
    ConfigFactory.parseString(ClientManagerSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import ClientManager._

  val peerManager = TestProbe()
  val addressManager = TestProbe()
  val params = MainNetParams
  val clientManagerRef = system.actorOf(ClientManager.props(peerManager.ref, addressManager.ref, params))

  override def afterAll {
    shutdown()
  }

  "A ClientManagerSpec" should {
    "request a new address from the address manager when make connection message is received" in {
      within(500 millis) {
        clientManagerRef ! ClientManager.MakeOutboundConnection()
        addressManager.expectMsg(AddressManager.GetAddress())
      }
    }
  }
}

object ClientManagerSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """
}