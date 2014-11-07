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
  val peerConnectionRef = system.actorOf(PeerConnection.props(null, null, testActor, remote, local, MainNetParams))

  override def afterAll {
    shutdown()
  }

  "A PeerConnection" should {
    "Forward a message it receives" in {
      within(500 millis) {
        peerConnectionRef ! PeerConnection.InitiateHandshake()
        expectMsg(Node.GetVersion(remote, local))
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
