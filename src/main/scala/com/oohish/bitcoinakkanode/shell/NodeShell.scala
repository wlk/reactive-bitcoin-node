package com.oohish.bitcoinakkanode.shell

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.language.postfixOps

import com.oohish.bitcoinakkanode.node.Node._
import com.oohish.bitcoinakkanode.node.SPVNode
import com.oohish.bitcoinakkanode.wire.MainNetParams

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

object NodeShell extends App {

  val prompt = "bitcoin-akka-node> "
  println("Welcome to bitcoin-akka-node.")

  val system = ActorSystem("bitcoin-akka-node")
  val node = system.actorOf(SPVNode.props(MainNetParams))
  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher
  import scala.util.control.Exception._

  def askNode(cmd: APICommand) = {
    val f = (node ? cmd)
    Await.result(f, 5 seconds) match {
      case Some(x) => x
      case None => "Not Found"
      case other => other
    }
  }

  var exiting: Boolean = false
  do {
    val line = StdIn.readLine(prompt)
    if (line == null) exiting = true else {
      val args = line.split("[\\s]+")

      args(0) match {
        case "getbestblockhash" =>
          val cmd = GetBestBlockHash()
          println(askNode(cmd))
        case "getblockcount" =>
          val cmd = GetBlockCount()
          println(askNode(cmd))
        case "getblockhash" =>
          if (args.length < 2) {
            println("Not enough args")
          } else {
            val tryCmd = for {
              index <- Try(args(1).toInt)
            } yield GetBlockHash(index)
            tryCmd match {
              case Success(cmd) => println(askNode(cmd))
              case Failure(ex) => println(s"Problem parsing args: ${ex.getMessage}")
            }
          }
        case "getconnectioncount" =>
          val cmd = GetConnectionCount()
          println(askNode(cmd))
        case "getpeerinfo" =>
          val cmd = GetPeerInfo()
          println(askNode(cmd))
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
}