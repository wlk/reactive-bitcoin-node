package com.oohish.chain

import scala.concurrent.Future

import com.oohish.peermessages.Block
import com.oohish.structures.char32

/*
 * Copied from bitcoinj's blockstore 
 */
trait BlockStore {

  def put(block: StoredBlock): Future[Unit]

  def get(hash: char32): Future[Option[StoredBlock]]

  def getChainHead(): Future[Option[StoredBlock]]

  def setChainHead(chainHead: StoredBlock): Future[Unit]

}