package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import com.oohish.bitcoinakkanode.node.Node
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.messages.Ping
import com.oohish.bitcoinscodec.structures.NetworkAddress

/**
 * a Test to show some TestKit examples
 */
class PeerConnectionSpec
  extends TestKit(ActorSystem("TestKitUsageSpec",
    ConfigFactory.parseString(TestKitUsageSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import TestKitUsageSpec._

  val remote = new InetSocketAddress(InetAddress.getLocalHost(), 1)
  val local = new InetSocketAddress(InetAddress.getLocalHost(), 2)
  val manager = TestProbe()
  val tcpConnection = TestProbe()
  val node = TestProbe()
  val peerConnectionRef = system.actorOf(PeerConnection.props(manager.ref, tcpConnection.ref, node.ref, remote, local, MainNetParams))

  override def afterAll {
    shutdown()
  }

  "A PeerConnection" should {
    "initiate a handshake when it receives an Initiate command" in {
      within(500 millis) {
        peerConnectionRef ! PeerConnection.InitiateHandshake()
        node.expectMsg(Node.GetVersion(remote, local))
        val v = Version(60000, 1, 12345L, NetworkAddress(1, remote), NetworkAddress(1, local), 5555L, "agent", 1, true)
        node.reply(v)
        tcpConnection.expectMsg(TCPConnection.OutgoingMessage(v))
      }
    }
  }
}

object TestKitUsageSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

}
