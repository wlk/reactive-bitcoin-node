package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.quantifind.sumac.FieldArgs

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

class NodeArgs extends FieldArgs {
  var network: String = "main"

  def getNetworkParams =
    network match {
      case "main" => MainNetParams
      case "test" => TestNet3Params
      case other  => throw new IllegalStateException(s"Invalid network name: $other")
    }
}

trait CLI {

  def getAPICommand(input: String): Try[Node.APICommand] = {
    val cmdpattern = """([^\s]+)(.*)""".r
    input match {
      case cmdpattern("getpeerinfo", param) =>
        Success(Node.GetPeerInfo)
      case cmdpattern("getconnectioncount", param) =>
        Success(Node.GetConnectionCount)
      case _ =>
        Failure(new IllegalStateException(s"cmd not found: $input"))
    }
  }

  def handleCommand(node: ActorRef, input: String) = {
    getAPICommand(input) match {
      case Success(cmd) =>
        println(queryNode(node, cmd))
      case Failure(e) =>
        println(e.getMessage)
    }
  }

  def handleInputs(node: ActorRef): Unit = {
    var ok = true
    do {
      print("reactive-bitcoin-node>")
      val ln = scala.io.StdIn.readLine
      ok = ln != null && ln != "quit" && ln != "exit"
      if (ok && !ln.isEmpty())
        handleCommand(node, ln)
    } while (ok)
  }

  def queryNode(node: ActorRef, cmd: Node.APICommand): Node.APIResponse

}

object Shell extends CLI {

  def main(args: Array[String]) {
    val sys = ActorSystem("shellsys")

    val nodeArgs = new NodeArgs
    try {
      nodeArgs.parse(args)
      println(s"Starting bitcoin node on network: ${nodeArgs.network}")
      val node = sys.actorOf(Node.props(nodeArgs.getNetworkParams))
      handleInputs(node)
      println(s"Shutting down bitcoin node")
    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }

    sys.shutdown
  }

  override def queryNode(node: ActorRef, cmd: Node.APICommand): Node.APIResponse = {
    val t = 10 seconds
    implicit val timeout = Timeout(t)
    val f = (node ? cmd).mapTo[Node.APIResponse]
    Await.result(f, t)
  }

}
