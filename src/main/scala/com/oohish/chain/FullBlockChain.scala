package com.oohish.chain

import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Verack
import com.oohish.structures.uint32_t
import com.oohish.wire.BTCConnection.Outgoing
import com.oohish.wire.NetworkParameters
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.pattern.pipe
import reactivemongo.api.MongoConnection
import com.oohish.peermessages.GetBlocks
import com.oohish.peermessages.Inv
import com.oohish.peermessages.Block
import akka.actor.ActorRef
import com.oohish.structures.InvVect
import com.oohish.structures.InvType
import scala.util.Success
import com.oohish.peermessages.GetData
import scala.concurrent.Await

object FullBlockChain {

  def props(
    networkParams: NetworkParameters,
    conn: Option[MongoConnection]) =
    Props(classOf[FullBlockChain], networkParams, conn)

  case object Initialized

}

class FullBlockChain(
  networkParams: NetworkParameters,
  conn: Option[MongoConnection]) extends Actor with ActorLogging {

  import context.dispatcher
  import FullBlockChain.Initialized

  // initialize the block store.
  val store: BlockStore = new MongoBlockStore(conn)
  val blockChain = new BlockChain(store)
  val futureInitialized = blockChain.addBlock(networkParams.genesisBlock, true)
  futureInitialized.onComplete { case _ => context.parent ! Initialized }

  def receive = listening()

  def listening(): Receive = {

    case Verack() => {
      log.info("FullBlockChain received Verack")
      val s = sender
      log.info("Becoming syncing")
      context.become(syncing(s))

      val futureBL = Chain.blockLocator(store)
      val blockLocator = futureBL.map { bl =>
        Outgoing(
          GetBlocks(networkParams.PROTOCOL_VERSION, bl, Chain.emptyHashStop))
      }
      blockLocator.pipeTo(sender)
    }

    case b: Block => {
      val futureAdded = blockChain.addBlock(b)
    }

  }

  def syncing(peer: ActorRef): Receive = {

    case Inv(vectors) => {
      //log.info("FullBlockChain received Inv")
      val s = sender
      if (sender == peer) {
        if (vectors.forall(_.t.name == "MSG_BLOCK")) {
          if (vectors.isEmpty) {
            log.info("Finished syncing. Becoming listening.")
            context.become(listening())
          } else {
            log.info("Becoming downloading with invs: " + vectors.length)
            context.become(downloading(s, vectors))
            sender ! Outgoing(GetData(vectors))
          }
        }
      }
    }

  }

  def downloading(peer: ActorRef, invs: List[InvVect]): Receive = {

    case b: Block => {
      import scala.concurrent.duration._
      import scala.language.postfixOps

      val s = sender

      if (sender == peer) {

        log.info("received block: " + b.hash)
        val futureAdded = blockChain.addBlock(b)
        val newInvs = invs.filter(inv => inv.hash != b.hash)

        // ugly, but necessary.
        Await.result(futureAdded, 10 seconds)

        if (newInvs.isEmpty) {
          log.info("Becoming syncing again-------------------------------------")
          context.become(syncing(peer))
          val futureBL = Chain.blockLocator(store)
          val blockLocator = futureBL.map { bl =>
            Outgoing(
              GetBlocks(networkParams.PROTOCOL_VERSION, bl, Chain.emptyHashStop))
          }
          blockLocator.pipeTo(peer)
        } else {
          log.info("Becoming downloading with invs: " + newInvs.length)
          context.become(downloading(peer, newInvs))
        }
      }
    }

  }

}