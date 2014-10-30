bitcoin-akka-node
==============
[![Build Status](https://travis-ci.org/yzernik/bitcoin-akka-node.svg?branch=master)](https://travis-ci.org/yzernik/bitcoin-akka-node)
[![Coverage Status](https://img.shields.io/coveralls/yzernik/bitcoin-akka-node.svg)](https://coveralls.io/r/yzernik/bitcoin-akka-node?branch=master)
[![tip for next commit](https://tip4commit.com/projects/1006.svg)](https://tip4commit.com/github/yzernik/bitcoin-akka-node)

**Disclaimer: This is experimental software. Do not use it with real money.**

Requirements
--------------
- sbt

How to run on testnet
--------------
```
sbt
```

and then

```
run -n test
```

How to run on mainnet
--------------
```
sbt
```

and then

```
run
```

Commands
--------------
Some of the API commands of the [original Bitcoin client](https://en.bitcoin.it/wiki/Original_Bitcoin_client/API_calls_list) are supported.

For example,

```
bitcoin-akka-node> getconnectioncount
10
```

or

```
bitcoin-akka-node> getblockhash 73546
000000000099ae23ec45ae651c5fa6cdc3505e20e5daf6a3c33b65e05311839c
```

etc.


Get help
--------------
```
bitcoin-akka-node> help
...
```