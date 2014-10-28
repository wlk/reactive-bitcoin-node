package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.spv.SPVBlockDownloader
import akka.actor.Actor

trait HeadersDownloaderComponent {
  this: Actor with Node with BlockChainComponent =>

  //val downloader = context.actorOf(SPVBlockDownloader.props(self, blockchain, pm, networkParams), "headers-downloader")

}