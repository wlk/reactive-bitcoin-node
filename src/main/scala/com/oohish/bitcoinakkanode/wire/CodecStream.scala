package com.oohish.bitcoinakkanode.wire

import scodec.codecs
import scodec.stream.{ encode, decode, StreamDecoder, StreamEncoder }
import scalaz.concurrent.Task
import com.oohish.bitcoinscodec.structures.Message
import com.oohish.bitcoinscodec.structures.Message.Message

object CodecStream {

  // decoder and encoder
  val d: StreamDecoder[Message] = decode.many(Message.codec(0L))
  val e: StreamEncoder[Message] = encode.many(Message.codec(0L))

}