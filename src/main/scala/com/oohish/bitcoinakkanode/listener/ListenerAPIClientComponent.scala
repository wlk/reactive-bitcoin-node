package com.oohish.bitcoinakkanode.listener

import com.oohish.bitcoinakkanode.node.APIClientComponent
import com.oohish.bitcoinakkanode.node.NetworkParamsComponent

import akka.actor.Actor

trait ListenerAPIClientComponent extends APIClientComponent {
  this: Actor with NetworkParamsComponent =>
  import com.oohish.bitcoinakkanode.listener._

  val apiClient = context.actorOf(ListenerAPIClient.props, "listener-api-client")

}