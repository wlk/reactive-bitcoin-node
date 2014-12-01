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
 * a Test to show some TestKit examples
 */
class HandshakerSpec
  extends TestKit(ActorSystem("HandshakerSpec",
    ConfigFactory.parseString(HandshakerSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import HandshakerSpec._

  val remote = new InetSocketAddress(InetAddress.getLocalHost(), 1)
  val local = new InetSocketAddress(InetAddress.getLocalHost(), 2)
  val tcpConn = TestProbe()
  val proxy = TestProbe()
  val params = MainNetParams
  val parent = system.actorOf(Props(new Actor {
    val handshakerRef = context.actorOf(Handshaker.props(tcpConn.ref, remote, local, params), "handshaker")
    def receive = {
      case x if sender == handshakerRef => proxy.ref forward x
      case x => handshakerRef forward x
    }
  }))

  override def afterAll {
    shutdown()
  }

  "A Handshaker" should {
    "initiate a handshake when it receives an Initiate command" in {

      within(500 millis) {
        proxy.send(parent, Handshaker.InitiateHandshake())
        val v1 = Version(60001, 1, 12345L, NetworkAddress(1, remote), NetworkAddress(1, local), 5555L, "agent1", 1, true)
        proxy.send(parent, v1)
        proxy.send(parent, Verack())
        proxy.expectMsg(FinishedHandshake())
      }
    }
  }
}

object HandshakerSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

}