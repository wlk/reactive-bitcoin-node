package com.oohish.wire

import java.net.InetSocketAddress

case class Peer(address: InetSocketAddress) {
  def hostAddress = address.getAddress.getHostAddress
  def port = address.getPort

  override def toString(): String = hostAddress + ":" + port
}
