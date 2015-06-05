package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.ActorSystem

object Driver extends App {

  val sys = ActorSystem("mysys")

  val params: NetworkParameters = MainNetParams

  sys.actorOf(Node.props(params))

}