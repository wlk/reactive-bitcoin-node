package com.oohish.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.PipelineContext
import akka.io.PipelineFactory
import akka.io.Tcp.Close
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.ConnectionClosed
import akka.io.Tcp.Received
import akka.io.Tcp.Write
import com.oohish.peermessages.MessagePayload

object TCPConnection {
  def props(network: String, peer: Peer, node: ActorRef, manager: ActorRef, connection: ActorRef) =
    Props(classOf[TCPConnection], network, peer, node, manager, connection)
}

class TCPConnection(network: String, peer: Peer, node: ActorRef, manager: ActorRef, connection: ActorRef) extends Actor with ActorLogging {
  import akka.actor.Terminated

  val ctx = new PipelineContext {}

  val btcConnection = context.actorOf(BTCConnection.props(peer, node, manager))
  context.watch(btcConnection)

  val pipeline =
    PipelineFactory.buildWithSinkFunctions(ctx, new peermessagestage(network) >> new MessageTypeStage)(
      cmd => connection ! Write(cmd.get),
      evt => btcConnection ! evt.get)

  def receive = {
    case msg: MessagePayload =>
      log.debug("sending message: " + msg)
      pipeline.injectCommand(msg)
    case CommandFailed(w: Write) =>
      // O/S buffer was full
      log.debug("write failed")
    case Received(data) =>
      //log.debug("received bytes:" + data)
      pipeline.injectEvent(data)
    case Terminated(btcConnection) =>
      connection ! Close
    case "close" =>
      connection ! Close
    case _: ConnectionClosed =>
      log.debug("connection closed")
      context stop self
  }

}