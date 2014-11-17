package com.oohish.bitcoinakkanode.shell

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.oohish.bitcoinakkanode.node.Node.APICommand
import com.oohish.bitcoinakkanode.node.Node.GetBestBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetBlockCount
import com.oohish.bitcoinakkanode.node.Node.GetBlockHash
import com.oohish.bitcoinakkanode.node.Node.GetConnectionCount
import com.oohish.bitcoinakkanode.node.Node.GetPeerInfo
import com.oohish.bitcoinakkanode.spv.SPVNode
import com.oohish.bitcoinakkanode.wire.MainNetParams
import com.oohish.bitcoinakkanode.wire.NetworkParameters
import com.oohish.bitcoinakkanode.wire.TestNet3Params

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

object NodeShell {

  implicit val timeout = Timeout(5 seconds)

  case class Config(network: NetworkParameters = MainNetParams,
    nodeProps: (NetworkParameters) => Props = SPVNode.props)

  val parser = new scopt.OptionParser[Config]("scopt") {
    head("bitcoin-akka-node", "0.1.0")
    opt[String]('n', "network name") action {
      case ("main", c) =>
        c.copy(network = MainNetParams)
      case ("test", c) =>
        c.copy(network = TestNet3Params)
    } text ("network is a String property")
    opt[String]('t', "node type") action {
      case ("spv", c) =>
        c.copy(nodeProps = SPVNode.props)
    } text ("node type is a String property")
  }

  def askNode(node: ActorRef, cmd: APICommand) = {
    val f = (node ? cmd)
    Await.result(f, 5 seconds) match {
      case Some(x) => x
      case None => "Not Found"
      case other => other
    }
  }

  def main(args: Array[String]) = {
    parser.parse(args, Config()) map { config =>
      val prompt = "bitcoin-akka-node> "
      println("Welcome to bitcoin-akka-node.")
      val system = ActorSystem("bitcoin-akka-node")
      val node = system.actorOf(config.nodeProps(
        config.network), "node")
      var exiting: Boolean = false
      do {
        val line = StdIn.readLine(prompt)
        if (line == null) exiting = true else {
          val args = line.split("[\\s]+")

          args(0) match {
            case "getbestblockhash" =>
              val cmd = GetBestBlockHash()
              println(askNode(node, cmd))
            case "getblockcount" =>
              val cmd = GetBlockCount()
              println(askNode(node, cmd))
            case "getblockhash" =>
              if (args.length < 2) {
                println("Not enough args")
              } else {
                val tryCmd = for {
                  index <- Try(args(1).toInt)
                } yield GetBlockHash(index)
                tryCmd match {
                  case Success(cmd) => println(askNode(node, cmd))
                  case Failure(ex) => println(s"Problem parsing args: ${ex.getMessage}")
                }
              }
            case "getconnectioncount" =>
              val cmd = GetConnectionCount()
              println(askNode(node, cmd))
            case "getpeerinfo" =>
              val cmd = GetPeerInfo()
              println(askNode(node, cmd))
            case "exit" => exiting = true
            case "quit" => exiting = true
            case "help" => println(
              """|These commands are available in this shell:
             |  getbestblockhash         Returns the hash of the best (tip) block in the longest block chain.
             |  getblockcount            Returns the number of blocks in the longest block chain.
             |  getblockhash             Returns hash of block in best-block-chain at <index>; index 0 is the genesis block.
             |  getconnectioncount       Returns the number of connections to other nodes.
             |  getpeerinfo              Returns data about each connected node.
             |  exit | quit              Exit this shell.
             |  help                     Display this help.""".stripMargin)

            case _ => if (args.size > 0 && line != "") println(s"unknown command: ${args(0)}")
          }
        }
      } while (!exiting)
      system.shutdown()
    } getOrElse {
      // arguments are bad, error message will have been displayed
    }
  }
}