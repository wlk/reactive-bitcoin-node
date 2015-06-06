package io.github.yzernik.reactivebitcoinnode.node

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
  val sys = ActorSystem("shellsys")

  def main(args: Array[String]) {
    val nodeArgs = new NodeArgs
    try {
      nodeArgs.parse(args)
      println(s"Starting bitcoin node on network: ${nodeArgs.network}")
      sys.actorOf(Node.props(nodeArgs.getNetworkParams))

    } catch {
      case e: com.quantifind.sumac.FeedbackException =>
        println(nodeArgs.helpMessage)
    }
  }
}
