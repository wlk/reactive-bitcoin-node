package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.NetworkParameters

trait HasNetworkParams {

  def networkParams: NetworkParameters

}