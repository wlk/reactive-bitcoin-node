reactive-bitcoin-node
==============
[![Build Status](https://travis-ci.org/yzernik/reactive-bitcoin-node.svg?branch=master)](https://travis-ci.org/yzernik/reactive-bitcoin-node)
[![tip for next commit](https://tip4commit.com/projects/1006.svg)](https://tip4commit.com/github/yzernik/reactive-bitcoin-node)

**Disclaimer: This is experimental software. Do not use it with real money.**

Requirements
--------------
- sbt

How to run on testnet
--------------
```
sbt "run --network test"
```

How to run on mainnet
--------------
```
sbt "run --network main"
```

or

```
sbt run
```

Commands
--------------
Some of the API commands of the [original Bitcoin client](https://en.bitcoin.it/wiki/Original_Bitcoin_client/API_calls_list) are supported through the interactive shell.

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


Get help
--------------
```
reactive-bitcoin-node> help
...
```

About
--------------
[bitcoin-scodec](https://github.com/yzernik/bitcoin-scodec) is used for encoding/decoding of network peer messages.
