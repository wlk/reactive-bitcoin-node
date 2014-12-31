package com.github.yzernik.reactivebitcoinnode.node

import akka.actor.ActorSystem

object Driver extends App {

  val sys = ActorSystem("mysys")

  sys.actorOf(NetworkController.props())

}