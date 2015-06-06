package io.github.yzernik.reactivebitcoinnode.node

import akka.actor.ActorSystem

object Driver {

  val sys = ActorSystem("mysys")

  val params: NetworkParameters = TestNet3Params

  sys.actorOf(Node.props(params))

}