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
import com.oohish.bitcoinakkanode.node.Node
import com.oohish.bitcoinscodec.messages.Verack
import com.oohish.bitcoinscodec.messages.Version
import com.oohish.bitcoinscodec.structures.NetworkAddress
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.DefaultTimeout
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory

/**
 * a Test to show some TestKit examples
 */
class PeerConnectionSpec
  extends TestKit(ActorSystem("PeerConnectionSpec",
    ConfigFactory.parseString(PeerConnectionSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import PeerConnectionSpec._

  val remote = new InetSocketAddress(InetAddress.getLocalHost(), 1)
  val local = new InetSocketAddress(InetAddress.getLocalHost(), 2)
  val manager = TestProbe()
  val tcpConnection = TestProbe()
  val peerConnectionRef = system.actorOf(PeerConnection.props(manager.ref, tcpConnection.ref, remote, local, MainNetParams))

  override def afterAll {
    shutdown()
  }

  "A PeerConnection" should {
    "initiate a handshake when it receives an Initiate command" in {
      within(500 millis) {
        peerConnectionRef ! PeerConnection.InitiateHandshake()
        // node.expectMsg(Node.GetVersion(remote, local)) TODO: expect something else
        val v1 = Version(60001, 1, 12345L, NetworkAddress(1, remote), NetworkAddress(1, local), 5555L, "agent1", 1, true)
        // node.reply(v1) TODO: reply something else
        tcpConnection.expectMsg(TCPConnection.OutgoingMessage(v1))
        val v2 = Version(60002, 1, 12346L, NetworkAddress(1, local), NetworkAddress(1, remote), 77777L, "agent2", 1, true)
        tcpConnection.reply(v2)
        tcpConnection.reply(Verack())
        tcpConnection.expectMsg(TCPConnection.OutgoingMessage(Verack()))
      }
    }
  }
}

object PeerConnectionSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

}
