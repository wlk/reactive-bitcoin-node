package com.oohish.chain

import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.oohish.structures.char32
import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONWriter
import reactivemongo.bson.BSONString
import reactivemongo.bson.BSONInteger
import com.oohish.util.HexBytesUtil
import reactivemongo.bson.BSONObjectID
import com.oohish.peermessages.Block
import com.oohish.structures.uint32_t
import play.api.libs.json.Writes
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import com.oohish.structures.VarStruct
import com.oohish.peermessages.Tx

class MongoBlockStore(
  conn: Option[MongoConnection]) extends BlockStore {

  val connection: MongoConnection = conn.getOrElse {
    // gets an instance of the driver
    // (creates an actor system)
    val driver = new MongoDriver
    driver.connection(List("localhost"))
  }

  // Gets a reference to the database "plugin"
  val db = connection("plugin")

  // Gets a reference to the collection "blocks"
  // By default, you get a BSONCollection.
  val collection = db("blocks")

  val blockMap: HashMap[char32, StoredBlock] = HashMap.empty[char32, StoredBlock]

  var chainHead: Option[StoredBlock] = None

  def put(block: StoredBlock): Future[Unit] =
    Future {
      //blockMap.put(Chain.blockHash(block.block), block)

      /*
      val x = Hi(1, "abc")

      val document = BSONDocument("b" -> x)*/

      blockMap.put(block.block.hash, block)
      ()
    }

  def get(hash: char32): Future[Option[StoredBlock]] =
    Future(blockMap.get(hash))

  def getChainHead(): Option[StoredBlock] =
    chainHead

  def setChainHead(cHead: StoredBlock): Unit =
    chainHead = Some(cHead)

}

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._

}