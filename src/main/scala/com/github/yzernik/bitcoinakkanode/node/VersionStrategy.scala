package com.github.yzernik.bitcoinakkanode.node

import java.net.InetSocketAddress

import scala.BigInt
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.joda.time.DateTime

import com.github.yzernik.bitcoinscodec.messages.Version
import com.github.yzernik.bitcoinscodec.structures.NetworkAddress

trait VersionStrategy {

  def getVersion(remote: InetSocketAddress,
    local: InetSocketAddress)(implicit executor: ExecutionContext): Future[Version] =
    for (height <- getHeight)
      yield Version(protocolVersion, services, timeStamp,
      NetworkAddress(services, remote), NetworkAddress(services, local),
      nonce, userAgent, height, relay)

  def protocolVersion: Int = 70002

  def services: BigInt

  def timeStamp: Long = {
    val millis = DateTime.now().getMillis()
    millis / 1000
  }

  def nonce: BigInt = {
    val bytes: Array[Byte] = Array.fill(8)(0)
    Random.nextBytes(bytes)
    BigInt(0.toByte +: bytes)
  }

  def userAgent: String = "/reactive-bitcoin-node:0.1.0/"

  def getHeight: Future[Int]

  def relay: Boolean

}