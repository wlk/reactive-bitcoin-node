import com.oohish.wire.Node

import akka.actor.ActorSystem

object SPVNode extends App {

  val system = ActorSystem("node")

  //start the node
  // val node = system.actorOf(Node.props(MainNetParams, true))

}