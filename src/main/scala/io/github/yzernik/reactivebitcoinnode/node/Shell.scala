package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

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

class CLI(node: ActorRef, implicit val ec: ExecutionContext) {

  implicit val timeout = Timeout(10 seconds)

  def getAPIResult(input: String): Future[Any] = {
    val cmdpattern = """[\s]*([^\s]+)(.*)""".r
    input match {
      case cmdpattern("getpeerinfo", param) =>
        queryNode(Node.GetPeerInfo)
      case cmdpattern("getconnectioncount", param) =>
        queryNode(Node.GetConnectionCount)
      case cmdpattern("getblockcount", param) =>
        queryNode(Node.GetBlockCount)
      case _ =>
        Future.failed(new IllegalStateException(s"command not found: $input"))
    }
  }

  private def queryNode(cmd: Node.APICommand) = node ? cmd

  def getCLIResult(input: String) =
    getAPIResult(input).map {
      res => res.toString
    }.recover {
      case e => e.getMessage
    }

  def evalCommand(input: String) = {
    val f = getCLIResult(input)
    Await.result(f, 10 seconds)
  }

  def handleInputs: Unit = {
    var ok = true
    do {
      print("reactive-bitcoin-node>")
      val ln = scala.io.StdIn.readLine
      ok = ln != null && ln != "quit" && ln != "exit"
      if (ok && !ln.isEmpty())
        println(evalCommand(ln))
    } while (ok)
  }

}

object Shell {

  def main(args: Array[String]) {
    val sys = ActorSystem("shellsys")

    val nodeArgs = new NodeArgs
    try {
      nodeArgs.parse(args)
      println(s"Starting bitcoin node on network: ${nodeArgs.network}")
      val node = sys.actorOf(Node.props(nodeArgs.getNetworkParams), name = "node")
      val cli = new CLI(node, sys.dispatcher)
      cli.handleInputs
      println(s"Shutting down bitcoin node")
    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }

    sys.shutdown
  }

}
