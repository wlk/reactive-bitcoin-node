package example

import com.oohish.chain.SPVBlockChain
import com.oohish.wire.Node

import akka.actor.ActorSystem

object SimpleNode {

  val system = ActorSystem("node")

  //start the header chain store
  val listener = system.actorOf(SPVBlockChain.props)

  //start the node
  val node = system.actorOf(Node.props(listener))

}