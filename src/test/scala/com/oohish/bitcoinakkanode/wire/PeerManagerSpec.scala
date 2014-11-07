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
class PeerManagerSpec
  extends TestKit(ActorSystem("PeerManagerSpec",
    ConfigFactory.parseString(PeerManagerSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  import PeerConnectionSpec._

  val node = TestProbe()
  val networkParams = MainNetParams
  val peerManagerRef = system.actorOf(PeerManager.props(node.ref, networkParams))

  override def afterAll {
    shutdown()
  }

  "A PeerManager" should {
    "include an address in the list of connected peers after becoming connected" in {
      within(5 seconds) {
        val peerConnection = TestProbe()
        val addr = new InetSocketAddress(InetAddress.getLocalHost(), 1)
        val local = new InetSocketAddress(InetAddress.getLocalHost(), 2)
        val v = Version(60001, 1, 12345L, NetworkAddress(1, addr), NetworkAddress(1, local), 5555L, "agent1", 1, true)
        peerManagerRef ! PeerManager.PeerConnected(peerConnection.ref, addr, v)
        peerManagerRef ! PeerManager.GetPeers()
        val peers = expectMsgType[List[(Long, InetSocketAddress)]]
        peers.map(_._2) should contain(addr)
      }
    }
  }
}

object PeerManagerSpec {
  // Define your test specific configuration here
  val config = """
    akka {
      loglevel = "WARNING"
    }
    """

}
