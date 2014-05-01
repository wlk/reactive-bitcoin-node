package com.oohish.wire

import java.nio.ByteOrder

import scala.annotation.tailrec

import com.oohish.peermessages.Addr
import com.oohish.peermessages.Alert
import com.oohish.peermessages.Block
import com.oohish.peermessages.GetAddr
import com.oohish.peermessages.GetBlocks
import com.oohish.peermessages.GetData
import com.oohish.peermessages.GetHeaders
import com.oohish.peermessages.Headers
import com.oohish.peermessages.Inv
import com.oohish.peermessages.MemPool
import com.oohish.peermessages.MessagePayload
import com.oohish.peermessages.NotFound
import com.oohish.peermessages.Ping
import com.oohish.peermessages.Pong
import com.oohish.peermessages.Tx
import com.oohish.peermessages.Verack
import com.oohish.peermessages.Version
import com.oohish.structures.uint32_t

import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import akka.io.SymmetricPipelineStage
import akka.util.ByteIterator
import akka.util.ByteString

/**
 * TODO
 */
object Pipeline {
  /**
   *  See https://en.bitcoin.it/wiki/Protocol_Specification#Message_structure
   */
  implicit val byteOrder = ByteOrder.BIG_ENDIAN
  val magicBytes = 4
  val commandBytes = 12
  val lengthBytes = 4
  val checksumBytes = 4

  val headerSize = magicBytes + commandBytes + lengthBytes + checksumBytes

  val maxSize = 1000000 // TODO ???
}

class MessageTypeStage extends SymmetricPipelineStage[PipelineContext, PartialMessage, ByteString] {
  import Pipeline._

  override def apply(ctx: PipelineContext) =
    new SymmetricPipePair[PartialMessage, ByteString] {
      var buffer = None: Option[ByteString]

      def commandPipeline: PartialMessage => Iterable[Result] =
        { pm: PartialMessage =>
          val bb = ByteString.newBuilder
          bb ++= uint32_t(pm.magic).encode
          bb ++= ByteString(Message.commandString(pm.command))
          bb ++= uint32_t(pm.body.length).encode
          bb ++= uint32_t(Message.checksum(pm.body)).encode
          bb ++= pm.body
          ctx.singleCommand(bb.result())
        }

      /*
       * This is how events (reads) are transformed: append the received
       * ByteString to the buffer (if any) and extract the frames from the
       * result. In the end store the new buffer contents and return the
       * list of events (i.e. `Left(...)`).
       */
      override def eventPipeline: ByteString ⇒ Iterable[Result] =
        { bs: ByteString ⇒
          val data = if (buffer.isEmpty) bs else buffer.get ++ bs
          val (nb, frames) = extractPartials(data, Nil)
          buffer = nb
          /*
         * please note the specialized (optimized) facility for emitting
         * just a single event
         */
          frames match {
            case Nil ⇒ Nil
            case one :: Nil ⇒ ctx.singleEvent(one)
            case many ⇒ many reverseMap (Left(_))
          }
        }

      /**
       * Extract as many complete frames as possible from the given ByteString
       * and return the remainder together with the extracted frames in reverse
       * order.
       */
      @tailrec
      def extractPartials(bs: ByteString, acc: List[PartialMessage]) //
      : (Option[ByteString], Seq[PartialMessage]) = {
        if (bs.isEmpty) {
          (None, acc)
        } else if (bs.length < headerSize) {
          (Some(bs.compact), acc)
        } else {
          val it = bs.iterator

          val magic = uint32_t.decode(it).n

          val commandArray = Array.fill(commandBytes)(0x0.toByte)
          it.getBytes(commandArray)
          val command = Message.commandFromCommandString(new String(commandArray))

          val length = uint32_t.decode(it).n.toInt
          if (length < 0 || length > maxSize)
            throw new IllegalArgumentException(
              s"received invalid frame size $length (max = $maxSize)")

          val chksmExpected = uint32_t.decode(it).n

          val total = headerSize + length

          if (bs.length >= total) {
            val payload = bs.slice(headerSize, total)
            val chksm = Message.checksum(payload)
            if (chksm != chksmExpected)
              throw new IllegalArgumentException(
                s"checksum $chksm doesn't match expected checksum $chksmExpected.")

            val partial = PartialMessage(magic, command, payload)
            extractPartials(bs drop total, partial :: acc)
          } else {
            (Some(bs.compact), acc)
          }
        }
      }
    }
}

class peermessagestage(networkMagic: Long) extends SymmetricPipelineStage[PipelineContext, MessagePayload, PartialMessage] {
  import Pipeline._

  override def apply(ctx: PipelineContext) =
    new SymmetricPipePair[MessagePayload, PartialMessage] {

      def commandPipeline: MessagePayload => Iterable[Result] =
        { m: MessagePayload =>
          val command = m match {
            case _: Verack => "verack"
            case _: Version => "version"
            case _: Addr => "addr"
            case _: Inv => "inv"
            case _: GetData => "getdata"
            case _: NotFound => "notfound"
            case _: GetBlocks => "getblocks"
            case _: GetHeaders => "getheaders"
            case _: Tx => "tx"
            case _: Block => "block"
            case _: Headers => "headers"
            case _: GetAddr => "getaddr"
            case _: MemPool => "mempool"
            case _: Ping => "ping"
            case _: Pong => "pong"
            case _: Alert => "alert"
          }
          ctx.singleCommand(PartialMessage(networkMagic, command, m.encode))
        }

      def eventPipeline: PartialMessage => Iterable[Result] =
        { pm: PartialMessage =>
          {
            if (pm.magic != networkMagic)
              throw new IllegalArgumentException(
                s"message magic does not match network $networkMagic")

            val key: (String, ByteIterator) = (pm.command, pm.body.iterator)
            val singleEvent = parseMessageBody.andThen { msg =>
              ctx.singleEvent(msg)
            }
            singleEvent.applyOrElse[(String, ByteIterator), Iterable[Result]](key, _ => Iterable.empty[Result])
          }
        }

      def parseMessageBody: PartialFunction[(String, ByteIterator), MessagePayload] = {
        case ("verack", body) => Verack.decode(body)
        case ("version", body) => Version.decode(body)
        case ("addr", body) => Addr.decode(body)
        case ("inv", body) => Inv.decode(body)
        case ("getdata", body) => GetData.decode(body)
        case ("notfound", body) => NotFound.decode(body)
        case ("getblocks", body) => GetBlocks.decode(body)
        case ("getheaders", body) => GetHeaders.decode(body)
        case ("tx", body) => Tx.decode(body)
        case ("block", body) => Block.decode(body)
        case ("headers", body) => Headers.decode(body)
        case ("getaddr", body) => GetAddr.decode(body)
        case ("mempool", body) => MemPool.decode(body)
        case ("ping", body) => Ping.decode(body)
        case ("pong", body) => Pong.decode(body)
        case ("alert", body) => Alert.decode(body)
      }
    }
}