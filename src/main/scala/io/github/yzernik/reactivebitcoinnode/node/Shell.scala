package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.quantifind.sumac.FieldArgs

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout.durationToTimeout
import io.github.yzernik.btcio.actors.BTC.PeerInfo

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

  /**
   * Evaluate a single command line command.
   */
  def evalCommand(input: String): String = {
    val cmdpattern = """[\s]*([^\s]+)(.*)""".r
    input match {
      case cmdpattern("getpeerinfo", param) =>
        val r = queryNode(Node.GetPeerInfo).asInstanceOf[List[PeerInfo]]
        r.toString
      case cmdpattern("getconnectioncount", param) =>
        queryNode(Node.GetConnectionCount).asInstanceOf[Int]
          .toString
      case cmdpattern("getblockcount", param) =>
        queryNode(Node.GetBlockCount).asInstanceOf[Int]
          .toString
      case _ =>
        s"command not found: $input"
    }
  }

  /**
   * Ask the Bitcoin node for the result of the query.
   */
  private def queryNode(cmd: Node.APICommand) = {
    val t = (10 seconds)
    Await.result((node ? cmd)(t), t)
  }

  /**
   * Repeatedly handle command line commands until the exit.
   */
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
