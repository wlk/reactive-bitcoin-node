package com.oohish.peermessages

import akka.util.ByteString
import com.oohish.structures.Structure
import com.oohish.structures.StructureReader

trait MessagePayload extends Structure {

}

trait MessagePayloadReader[T <: MessagePayload] extends StructureReader[T] {

}