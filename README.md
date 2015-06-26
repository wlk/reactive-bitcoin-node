# reactive-bitcoin-node

[![Build Status](https://travis-ci.org/yzernik/reactive-bitcoin-node.svg?branch=master)](https://travis-ci.org/yzernik/reactive-bitcoin-node)
[![tip for next commit](https://tip4commit.com/projects/1006.svg)](https://tip4commit.com/github/yzernik/reactive-bitcoin-node)

Akka actor based bitcoin client.

Some of the API commands of the [original Bitcoin client](https://en.bitcoin.it/wiki/Original_Bitcoin_client/API_calls_list) are supported. More are in progress.

The bitcoin node can be used in Java or Scala code, or run through the command-line interface.

## Running in the shell

### Requirements

- sbt

### run on testnet

```
sbt "run --network test"
```

### run on mainnet

```
sbt "run --network main"
```

or

```
sbt run
```

### Commands

Commands can be run throught the shell.

For example,

```
reactive-bitcoin-node> getconnectioncount
10
```

or

```
reactive-bitcoin-node> getblockhash 73546
000000000099ae23ec45ae651c5fa6cdc3505e20e5daf6a3c33b65e05311839c
```

etc.


## About

[bitcoin-scodec](https://github.com/yzernik/bitcoin-scodec) is used for encoding/decoding of network peer messages.
