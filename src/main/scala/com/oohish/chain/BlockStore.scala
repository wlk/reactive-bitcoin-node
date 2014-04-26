package com.oohish.chain

import scala.concurrent.Future

import com.oohish.peermessages.Block
import com.oohish.structures.char32

/*
 * Copied from bitcoinj's blockstore 
 */
trait BlockStore {

  def put(block: Block): Future[Unit]

  def get(hash: char32): Future[Option[Block]]

  def getChainHead(): Future[Option[Block]]

  def setChainHead(chainHead: Block): Future[Unit]

}