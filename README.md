# reactive-bitcoin-node

[![Build Status](https://travis-ci.org/yzernik/reactive-bitcoin-node.svg?branch=master)](https://travis-ci.org/yzernik/reactive-bitcoin-node)
[![tip for next commit](https://tip4commit.com/projects/1006.svg)](https://tip4commit.com/github/yzernik/reactive-bitcoin-node)

Akka actor based bitcoin client.

Some of the API commands of the [original Bitcoin client](https://en.bitcoin.it/wiki/Original_Bitcoin_client/API_calls_list) are supported. More are in progress.

The bitcoin node can be used in Java or Scala, or run through the command-line interface.

## Usage

Create an instance of Bitcoin Node object with an actor system.

```
scala> import io.github.yzernik.reactivebitcoinnode.node._
scala> import akka.actor.ActorSystem
scala> implicit val sys = ActorSystem("sys")
scala> val node = new Node()
```

Optionally specify which network parameters to use.

```
scala> val node = new Node(TestNet3Params)
```

### Commands

API commands are available as asynchronous methods on the node object.

For example,

```
scala> import scala.concurrent.Future
scala> val count: Future[Int] = node.getBlockCount
```

to get information about the current state of the blockchain.

or

```
scala> import io.github.yzernik.btcio.actors.BTC._
scala> val peers: Future[List[PeerInfo]] = node.getPeerInfo
```

to get info about the currently connected peers.

etc.


## About

[bitcoin-scodec](https://github.com/yzernik/bitcoin-scodec) is used for encoding/decoding of network peer messages.
