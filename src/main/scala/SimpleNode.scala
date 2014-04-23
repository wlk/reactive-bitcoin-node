import com.oohish.wire.Node
import com.oohish.wire.TestNet3Params

import akka.actor.ActorSystem

object SimpleNode extends App {

  val system = ActorSystem("node")

  //start the node
  val node = system.actorOf(Node.props(TestNet3Params))

}