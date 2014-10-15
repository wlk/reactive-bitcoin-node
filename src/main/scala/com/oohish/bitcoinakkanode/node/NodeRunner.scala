package com.oohish.bitcoinakkanode.node

import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.language.postfixOps
import com.oohish.bitcoinakkanode.node.Node._
import com.oohish.bitcoinakkanode.wire.MainNetParams
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.oohish.bitcoinscodec.structures.Hash
import scala.concurrent.Await

object NodeRunner extends App {

  val prompt = "bitcoin-akka-node> "
  println("Welcome to bitcoin-akka-node.")

  val system = ActorSystem("bitcoin-akka-node")
  val node = system.actorOf(SPVNode.props(MainNetParams))
  implicit val timeout = Timeout(5 seconds)
  import system.dispatcher

  var exiting: Boolean = false
  do {
    val line = StdIn.readLine(prompt)
    if (line == null) exiting = true else {
      val args = line.split("[\\s]+")

      args(0) match {
        case "getbestblockhash" =>
          val f = (node ? GetBestBlockHash())
          println(Await.result(f, 5 seconds))
        case "getblockcount" =>
          val f = (node ? GetBlockCount())
          println(Await.result(f, 5 seconds))
        case "getconnectioncount" =>
          val f = (node ? GetConnectionCount())
          println(Await.result(f, 5 seconds))
        case "exit" => exiting = true
        case "quit" => exiting = true
        case "help" => println(
          """|These commands are available in this shell:
             |  getbestblockhash         Returns the hash of the best (tip) block in the longest block chain.
             |  getblockcount            Returns the number of blocks in the longest block chain.
             |  exit | quit              Exit this shell.
             |  help                     Display this help.""".stripMargin)

        case _ => if (args.size > 0 && line != "") println(s"unknown command: ${args(0)}")
      }

    }
  } while (!exiting)

  system.shutdown()
}