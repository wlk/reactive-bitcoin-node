package io.github.yzernik.reactivebitcoinnode.node

import com.quantifind.sumac.FieldArgs
import akka.actor.ActorSystem
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
import io.github.yzernik.bitcoinscodec.messages.Version
import io.github.yzernik.bitcoinscodec.structures.Hash
import io.github.yzernik.btcio.actors.BTC
import com.quantifind.sumac
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

trait CLI {

  def handleCommand(node: ActorRef, input: String) = {
    val cmdpattern = """([^\s]+)(.*)""".r
    input match {
      case cmdpattern(cmd, param) if cmd == "getpeerinfo" =>
        val c = Node.GetPeerInfo
        println(queryNode(node, c))
      case _ =>
        println(s"cmd not found: $input")
    }
  }

  def handleInputs(node: ActorRef): Unit = {
    var ok = true
    do {
      print("reactive-bitcoin-node>")
      val ln = scala.io.StdIn.readLine
      ok = ln != null && ln != "quit" && ln != "exit"
      if (!ln.isEmpty())
        handleCommand(node, ln)
    } while (ok)
  }

  def queryNode(node: ActorRef, cmd: Node.APICommand): Node.APIResponse

}

object Shell extends CLI {
  val sys = ActorSystem("shellsys")

  def main(args: Array[String]) {
    val nodeArgs = new NodeArgs
    try {
      nodeArgs.parse(args)
      println(s"Starting bitcoin node on network: ${nodeArgs.network}")
      val node = sys.actorOf(Node.props(nodeArgs.getNetworkParams))
      handleInputs(node)
      println(s"Stopping bitcoin node")
      sys.shutdown
    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }
  }

  override def queryNode(node: ActorRef, cmd: Node.APICommand): Node.APIResponse = {
    import sys.dispatcher
    val t = 5 seconds
    implicit val timeout = Timeout(t)
    val f = (node ? cmd).mapTo[Node.APIResponse]
    Await.result(f, t)
  }

}
