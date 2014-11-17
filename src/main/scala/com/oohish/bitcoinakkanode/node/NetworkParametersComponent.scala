package com.oohish.bitcoinakkanode.node

import com.oohish.bitcoinakkanode.wire.NetworkParameters

trait NetworkParametersComponent {

  val networkParameters: NetworkParameters

}