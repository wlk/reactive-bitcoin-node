package com.oohish.pool

import com.oohish.peermessages.Tx

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object MemoryPool {
  def props() =
    Props(classOf[MemoryPool])

  val MAX_BLOCK_SIZE = 1000000
  val MIN_OUT_VALUE = 0
  val Max_OUT_VALUE = 21000000L * 100000000L // 21 million BTC

  def validByteSize(tx: Tx): Boolean = {
    tx.encode.size < MAX_BLOCK_SIZE
  }

  def legalMoneyRange(tx: Tx): Boolean = {
    val outVals = tx.tx_out.seq.map(_.value)
    val sm = outVals.sum
    outVals.forall(outVal => outVal >= MIN_OUT_VALUE && outVal <= Max_OUT_VALUE) &&
      (sm >= MIN_OUT_VALUE && sm <= Max_OUT_VALUE)
  }

  def nonCoinbase(tx: Tx): Boolean = {
    tx.tx_in.seq.forall { txIn =>
      !(txIn.previous_output.hash == "0000000000000000000000000000000000000000000000000000000000000000" &&
        txIn.previous_output.index == Integer.MAX_VALUE)
    }
  }

  // Check that nLockTime <= INT_MAX[1], size in bytes >= 100[2], and sig opcount <= 2[3]
  def validNLockTime(tx: Tx): Boolean = {
    tx.lock_time <= Integer.MAX_VALUE &&
      tx.encode.size >= 100 &&
      true
  }

}

class MemoryPool extends Actor with ActorLogging {

  var transactionPool = Set.empty[Tx]

  def receive = {

    case tx: Tx => {

      // check if tx is valid

    }

    case _ =>
  }

}