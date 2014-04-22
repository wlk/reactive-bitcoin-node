package com.oohish.wire

import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.Verack
import com.oohish.peermessages.Version
import com.oohish.structures.int64_t
import com.oohish.wire.PeerManager.PeerConnected

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.actorRef2Scala

object BTCConnection {
  def props(peer: Peer, node: ActorRef, manager: ActorRef) =
    Props(classOf[BTCConnection], peer, node, manager)

  case class ConnectTimeout()
  case class Outgoing(m: MessagePayload)

}

class BTCConnection(peer: Peer, node: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  import BTCConnection._
  import akka.actor.Terminated
  import Node.Incoming

  context.parent ! Node.version(peer)

  def receive = connecting(false, None)

  def connecting(verackReceived: Boolean, versionReceived: Option[int64_t]): Receive = {

    case _: Verack => {
      if (versionReceived.isDefined) {
        finishHandshake(versionReceived.get)
      } else {
        context.become(connecting(true, None))
      }
    }

    case m: Version => {
      context.parent ! Node.verack
      if (verackReceived) {
        finishHandshake(m.timestamp)
      } else {
        context.parent ! Node.version(peer)
        context.become(connecting(false, Some(m.timestamp)))
      }
    }

    case m: ConnectTimeout => {
      context.stop(self)
    }

    case other => {
      log.debug("got other: " + other)
    }

  }

  def finishHandshake(time: int64_t): Unit = {
    manager ! PeerConnected(peer, time.n)
    node ! Verack()
    log.info("becoming connected")
    context.become(connected)
  }

  def connected(): Receive = {

    case Outgoing(m) => {
      log.debug("outgoing message: " + m)
      context.parent ! m
    }

    case m: MessagePayload => {
      node ! m
    }

    case Terminated(ref) => {
      context.stop(self)
    }

    case other => {
      log.warning("got other: " + other)
    }

  }

}