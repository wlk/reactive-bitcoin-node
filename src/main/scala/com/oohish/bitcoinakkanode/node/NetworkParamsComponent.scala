package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.NetworkParameters

trait NetworkParamsComponent {

  def networkParams: NetworkParameters

}