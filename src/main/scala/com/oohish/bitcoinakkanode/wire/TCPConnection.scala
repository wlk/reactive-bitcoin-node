package com.oohish.bitcoinakkanode.wire

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp.Close
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.ConnectionClosed
import akka.io.Tcp.Received
import akka.io.Tcp.Write
import com.oohish.bitcoinscodec.structures.Message._

object TCPConnection {
  def props(node: ActorRef, manager: ActorRef, connection: ActorRef) =
    Props(classOf[TCPConnection], node, manager, connection)
}

class TCPConnection(node: ActorRef, manager: ActorRef, connection: ActorRef) extends Actor with ActorLogging {
  import akka.actor.Terminated

  /*
    val btcConnection = context.actorOf(BTCConnection.props(peer, networkParams, node, manager))
  context.watch(btcConnection)
   
   * 
  val decoderprocess = ???
  val encoderprocess = ???
*/

  def receive = {
    case msg: Message =>
      log.debug("sending message: " + msg)
    //pipeline.injectCommand(msg)
    //encoderprocess.inject(msg)
    case CommandFailed(w: Write) =>
      // O/S buffer was full
      log.debug("write failed")
    case Received(data) =>
    //pipeline.injectEvent(data)
    //decoderprocess.inject?(data)
    case Terminated(btcConnection) =>
      connection ! Close
    case "close" =>
      connection ! Close
    case _: ConnectionClosed =>
      log.debug("connection closed")
      context stop self
  }

}