package com.oohish.bitcoinakkanode.full

object FullNode {
  //def props(networkParams: NetworkParameters) =
  //  Props(classOf[FullNode], networkParams)
}

/*
class FullNode(np: NetworkParameters) extends Node {
  import context.dispatcher

  def networkParams = np
  lazy val blockchain = context.actorOf(FullBlockChain.props(networkParams))

  def blockDownload(ref: ActorRef) = {
    log.debug("sending block locator")
    (blockchain ? BlockChain.GetBlockLocator())
      .mapTo[List[Hash]]
      .map(bl =>
        PeerManager.UnicastMessage(
          GetHeaders(networkParams.PROTOCOL_VERSION, bl), ref))
      .pipeTo(pm)
  }

  def msgReceive(from: ActorRef): PartialFunction[Message, Unit] = {
    case Headers(headers) =>
      log.debug("headers size: {}", headers.size)
      headers.foreach {
        blockchain ! BlockChain.PutBlock(_)
      }
      if (!headers.isEmpty) blockDownload(from)
    case other =>
      log.debug("node received other message: {}", other.getClass())
  }

}
*/