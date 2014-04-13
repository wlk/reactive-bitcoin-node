import com.oohish.wire.Node
import com.oohish.wire.SimplisticHandler
import akka.actor.Actor
import akka.actor.Props
import com.oohish.chain.SPVBlockChain

object HelloWorld {
  def props(): Props =
    Props(classOf[HelloWorld])
}

class HelloWorld extends Actor {

  override def preStart(): Unit = {

    //start a listener
    // val listener = context.actorOf(SimplisticHandler.props)
    val listener = context.actorOf(SPVBlockChain.props)

    //start the node
    val node = context.actorOf(Node.props(listener))

  }

  def receive = {
    // when the greeter is done, stop this actor and with it the application
    case _ ⇒
  }
}

