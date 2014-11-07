package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.io.Tcp
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.util.ByteString

/**
 * a Test to show some TestKit examples
 */
class TCPConnectionSpec
  extends TestKit(ActorSystem("TCPConnectionSpec",
    ConfigFactory.parseString(TCPConnectionSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import TCPConnectionSpec._

  val remote = new InetSocketAddress(InetAddress.getLocalHost(), 1)
  val local = new InetSocketAddress(InetAddress.getLocalHost(), 2)
  val manager = TestProbe()
  val conn = TestProbe()
  val node = TestProbe()
  val tcpConnectionRef = system.actorOf(TCPConnection.props(manager.ref, node.ref, conn.ref, remote, local, MainNetParams, true))

  override def afterAll {
    shutdown()
  }

  "A TCPConnection" should {
    "send an encoded message over the wire" in {
      within(500 millis) {
        val bytes = ByteString("0xDEADBEEF")
        tcpConnectionRef ! MessageEncoder.EncodedMessage(bytes)
        conn.expectMsg(Tcp.Write(bytes))
      }
    }

  }
}

object TCPConnectionSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

}
