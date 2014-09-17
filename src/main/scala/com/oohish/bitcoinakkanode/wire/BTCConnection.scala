package com.oohish.bitcoinakkanode.wire

import java.net.InetAddress
import java.net.InetSocketAddress
import scala.Array.canBuildFrom
import scala.math.BigInt.int2bigInt
import scala.util.Random
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala

object BTCConnection {
  def props(manager: ActorRef) =
    Props(classOf[BTCConnection], manager)
}

class BTCConnection(manager: ActorRef) extends Actor with ActorLogging {
  import BTCConnection._
  import com.oohish.bitcoinscodec.structures.Message._

  def receive = {
    case msg: Message => {
      sender ! msg
    }
  }

}