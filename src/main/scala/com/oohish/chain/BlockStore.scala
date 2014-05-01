package com.oohish.chain

import scala.concurrent.Future

/*
 * Copied from bitcoinj's blockstore 
 */
trait BlockStore {

  def put(block: StoredBlock): Future[Unit]

  def get(hash: String): Future[Option[StoredBlock]]

  def getChainHead(): Future[Option[StoredBlock]]

  def setChainHead(chainHead: StoredBlock): Future[Unit]

}