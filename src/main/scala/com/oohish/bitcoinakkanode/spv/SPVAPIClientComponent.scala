package com.oohish.bitcoinakkanode.spv

import com.oohish.bitcoinakkanode.node.APIClientComponent
import com.oohish.bitcoinakkanode.node.NetworkParamsComponent

import akka.actor.Actor

trait SPVAPIClientComponent extends APIClientComponent {
  this: Actor with NetworkParamsComponent =>

  val apiClient = context.actorOf(SPVAPIClient.props, "spv-api-client")

}