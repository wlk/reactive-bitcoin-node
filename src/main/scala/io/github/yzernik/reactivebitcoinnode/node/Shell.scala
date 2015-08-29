package io.github.yzernik.reactivebitcoinnode.node

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.quantifind.sumac.FieldArgs

import akka.actor.ActorSystem

class NodeArgs extends FieldArgs {
  var network: String = "main"

  def getNetworkParams =
    network match {
      case "main" => MainNetParams
      case "test" => TestNet3Params
      case other  => throw new IllegalStateException(s"Invalid network name: $other")
    }
}

object Shell {

  /**
   * Repeatedly handle command line commands until the exit.
   */
  def handleInputs(node: Node): Unit = {
    var ok = true
    do {
      print("reactive-bitcoin-node>")
      val ln = scala.io.StdIn.readLine
      ok = ln != null && ln != "quit" && ln != "exit"
      if (ok && !ln.isEmpty())
        println(evalCommand(node, ln))
    } while (ok)
  }

  /**
   * Evaluate a single command line command.
   */
  def evalCommand(node: Node, input: String): String = {
    val cmdpattern = """[\s]*([^\s]+)(.*)""".r
    input match {
      case cmdpattern("getpeerinfo", param) =>
        awaitRPC(???).toString
      case cmdpattern("getconnectioncount", param) =>
        awaitRPC(node.getConnectionCount).toString
      case cmdpattern("getblockcount", param) =>
        awaitRPC(node.getBlockCount).toString
      case _ =>
        s"command not found: $input"
    }
  }

  /**
   * Wait for the result of the RPC call.
   */
  private def awaitRPC[T](r: Future[T]) =
    Await.result(r, 10 seconds)

  def main(args: Array[String]) {
    val sys = ActorSystem("shellsys")
    val nodeArgs = new NodeArgs

    try {
      nodeArgs.parse(args)
      println(s"Starting bitcoin node on network: ${nodeArgs.network}")
      val node = new Node(nodeArgs.getNetworkParams, sys)
      handleInputs(node)
      println(s"Shutting down bitcoin node")
    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }

    sys.shutdown
  }

}
