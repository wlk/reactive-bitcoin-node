package io.github.yzernik.reactivebitcoinnode.node

import scala.BigInt
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import io.github.yzernik.bitcoinscodec.messages.Block
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import io.github.yzernik.btcio.actors.BTC.PeerInfo
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import com.quantifind.sumac.FieldArgs
import akka.actor.ActorSystem
import scala.concurrent.Await

class NodeArgs extends FieldArgs {
  var network: String = "main"

  def getNetworkParams =
    network match {
      case "main" => MainNetParams
      case "test" => TestNet3Params
      case other  => throw new IllegalStateException(s"Invalid network name: $other")
    }
}

class CLI(node: NodeObj) {
  import scala.concurrent.ExecutionContext.Implicits.global

  def getAPIResult(input: String): Future[Any] = {
    val cmdpattern = """[\s]*([^\s]+)(.*)""".r
    input match {
      case cmdpattern("getpeerinfo", param) =>
        node.getPeerInfo
      case cmdpattern("getconnectioncount", param) =>
        node.getConnectionCount
      case _ =>
        Future.failed(new IllegalStateException(s"command not found: $input"))
    }
  }

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
      val node = new NodeObj(nodeArgs.getNetworkParams, sys)
      val cli = new CLI(node)
      cli.handleInputs
      println(s"Shutting down bitcoin node")
    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }

    sys.shutdown
  }

}
