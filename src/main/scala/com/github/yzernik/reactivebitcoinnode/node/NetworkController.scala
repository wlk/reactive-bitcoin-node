package com.github.yzernik.reactivebitcoinnode.node

import java.net.InetAddress
import java.net.InetSocketAddress

import scala.math.BigInt.int2bigInt

import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.bitcoinscodec.structures.NetworkAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO

object NetworkController {
  def props() =
    Props(classOf[NetworkController])
}

class NetworkController extends Actor with ActorLogging {
  import context.system

  val magic = MainNetParams.packetMagic
  val manager = IO(new BTC(magic, BigInt(1L), "reactive-bitcoin"))
  //manager ! BTC.Bind(8334)

  def receive = {
    case other =>
      log.info(s"receive other: $other")
  }

}

object PeerHandler {
  def props(versionResource: ActorRef) =
    Props(classOf[PeerHandler], versionResource)

  case class HandlePeer(peer: ActorRef, remote: InetSocketAddress, local: InetSocketAddress)
}

class PeerHandler(versionResource: ActorRef)
  extends Actor with ActorLogging {
  import PeerHandler._

  def receive = ready

  def ready: Receive = {
    case HandlePeer(peer, remote, local) =>
      peer ! BTC.Register(self)
  }

  def handling(peer: ActorRef): Receive = {
    case x =>
      log.info(s"received: $x")
  }

}
