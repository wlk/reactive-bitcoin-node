import akka.actor.{ Props, ActorSystem }

object MainGreeter extends App {
  val system = ActorSystem("server")
  val service = system.actorOf(HelloWorld.props, "ServerActor")
}