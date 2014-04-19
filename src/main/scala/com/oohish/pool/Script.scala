package com.oohish.pool

object Script {

  // push value
  case object OP_0 extends Opcode(0x00)
  case object OP_FALSE extends Opcode(0x00)
  case object OP_PUSHDATA1 extends Opcode(0x4c)
  case object OP_PUSHDATA2 extends Opcode(0x4d)
  case object OP_PUSHDATA4 extends Opcode(0x4e)
  case object OP_1NEGATE extends Opcode(0x4f)
  case object OP_1 extends Opcode(0x51)
  case object OP_TRUE extends Opcode(0x51)
  case object OP_2 extends Opcode(0x52)
  case object OP_3 extends Opcode(0x53)
  case object OP_4 extends Opcode(0x54)
  case object OP_5 extends Opcode(0x55)
  case object OP_6 extends Opcode(0x56)
  case object OP_7 extends Opcode(0x57)
  case object OP_8 extends Opcode(0x58)
  case object OP_9 extends Opcode(0x59)
  case object OP_10 extends Opcode(0x5a)
  case object OP_11 extends Opcode(0x5b)
  case object OP_12 extends Opcode(0x5c)
  case object OP_13 extends Opcode(0x5d)
  case object OP_14 extends Opcode(0x5e)
  case object OP_15 extends Opcode(0x5f)
  case object OP_16 extends Opcode(0x60)

  // control
  case object OP_NOP extends Opcode(0x61)
  case object OP_VER extends Opcode(0x62)
  case object OP_IF extends Opcode(0x63)
  case object OP_NOTIF extends Opcode(0x64)
  case object OP_VERIF extends Opcode(0x65)
  case object OP_VERNOTIF extends Opcode(0x66)
  case object OP_ELSE extends Opcode(0x67)
  case object OP_ENDIF extends Opcode(0x68)
  case object OP_VERIFY extends Opcode(0x69)
  case object OP_RETURN extends Opcode(0x6a)

  // stack ops
  case object OP_TOALTSTACK extends Opcode(0x6b)
  case object OP_FROMALTSTACK extends Opcode(0x6c)
  case object OP_2DROP extends Opcode(0x6d)
  case object OP_2DUP extends Opcode(0x6e)
  case object OP_3DUP extends Opcode(0x6f)
  case object OP_2OVER extends Opcode(0x70)
  case object OP_2ROT extends Opcode(0x71)
  case object OP_2SWAP extends Opcode(0x72)
  case object OP_IFDUP extends Opcode(0x73)
  case object OP_DEPTH extends Opcode(0x74)
  case object OP_DROP extends Opcode(0x75)
  case object OP_DUP extends Opcode(0x76)
  case object OP_NIP extends Opcode(0x77)
  case object OP_OVER extends Opcode(0x78)
  case object OP_PICK extends Opcode(0x79)
  case object OP_ROLL extends Opcode(0x7a)
  case object OP_ROT extends Opcode(0x7b)
  case object OP_SWAP extends Opcode(0x7c)
  case object OP_TUCK extends Opcode(0x7d)

  // splice ops
  case object OP_CAT extends Opcode(0x7e)
  case object OP_SUBSTR extends Opcode(0x7f)
  case object OP_LEFT extends Opcode(0x80)
  case object OP_RIGHT extends Opcode(0x81)
  case object OP_SIZE extends Opcode(0x82)

  // bit logic
  case object OP_INVERT extends Opcode(0x83)
  case object OP_AND extends Opcode(0x84)
  case object OP_OR extends Opcode(0x85)
  case object OP_XOR extends Opcode(0x86)
  case object OP_EQUAL extends Opcode(0x87)
  case object OP_EQUALVERIFY extends Opcode(0x88)
  case object OP_RESERVED1 extends Opcode(0x89)
  case object OP_RESERVED2 extends Opcode(0x8a)

  // numeric
  case object OP_1ADD extends Opcode(0x8b)
  case object OP_1SUB extends Opcode(0x8c)
  case object OP_2MUL extends Opcode(0x8d)
  case object OP_2DIV extends Opcode(0x8e)
  case object OP_NEGATE extends Opcode(0x8f)
  case object OP_ABS extends Opcode(0x90)
  case object OP_NOT extends Opcode(0x91)
  case object OP_0NOTEQUAL extends Opcode(0x92)

  case object OP_ADD extends Opcode(0x93)
  case object OP_SUB extends Opcode(0x94)
  case object OP_MUL extends Opcode(0x95)
  case object OP_DIV extends Opcode(0x96)
  case object OP_MOD extends Opcode(0x97)
  case object OP_LSHIFT extends Opcode(0x98)
  case object OP_RSHIFT extends Opcode(0x99)

  case object OP_BOOLAND extends Opcode(0x9a)
  case object OP_BOOLOR extends Opcode(0x9b)
  case object OP_NUMEQUAL extends Opcode(0x9c)
  case object OP_NUMEQUALVERIFY extends Opcode(0x9d)
  case object OP_NUMNOTEQUAL extends Opcode(0x9e)
  case object OP_LESSTHAN extends Opcode(0x9f)
  case object OP_GREATERTHAN extends Opcode(0xa0)
  case object OP_LESSTHANOREQUAL extends Opcode(0xa1)
  case object OP_GREATERTHANOREQUAL extends Opcode(0xa2)
  case object OP_MIN extends Opcode(0xa3)
  case object OP_MAX extends Opcode(0xa4)

  case object OP_WITHIN extends Opcode(0xa5)

  // crypto
  case object OP_RIPEMD160 extends Opcode(0xa6)
  case object OP_SHA1 extends Opcode(0xa7)
  case object OP_SHA256 extends Opcode(0xa8)
  case object OP_HASH160 extends Opcode(0xa9)
  case object OP_HASH256 extends Opcode(0xaa)
  case object OP_CODESEPARATOR extends Opcode(0xab)
  case object OP_CHECKSIG extends Opcode(0xac)
  case object OP_CHECKSIGVERIFY extends Opcode(0xad)
  case object OP_CHECKMULTISIG extends Opcode(0xae)
  case object OP_CHECKMULTISIGVERIFY extends Opcode(0xaf)

  // expansion
  case object OP_NOP1 extends Opcode(0xb0)
  case object OP_NOP2 extends Opcode(0xb1)
  case object OP_NOP3 extends Opcode(0xb2)
  case object OP_NOP4 extends Opcode(0xb3)
  case object OP_NOP5 extends Opcode(0xb4)
  case object OP_NOP6 extends Opcode(0xb5)
  case object OP_NOP7 extends Opcode(0xb6)
  case object OP_NOP8 extends Opcode(0xb7)
  case object OP_NOP9 extends Opcode(0xb8)
  case object OP_NOP10 extends Opcode(0xb9)

  // template matching params
  case object OP_SMALLDATA extends Opcode(0xf9)
  case object OP_SMALLINTEGER extends Opcode(0xfa)
  case object OP_PUBKEYS extends Opcode(0xfb)
  case object OP_PUBKEYHASH extends Opcode(0xfd)
  case object OP_PUBKEY extends Opcode(0xfe)

  case object OP_INVALIDOPCODE extends Opcode(0xff)

  // opcode types
  sealed class Opcode(code: Int) {

  }

  /**
   * The number of signature operands in the signature.
   */
  def GetSigOpCount(script: List[Byte]): Int = {

    0
  }

  /**
   * The next item in the script.
   */
  def nextScriptItem(script: List[Byte]): List[Byte] = {

    List.empty
  }

}