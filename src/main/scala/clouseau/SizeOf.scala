package clouseau

import java.lang.reflect.Field

sealed abstract class SizeOf extends Product with Serializable

object SizeOf {

  case class Constant(n: Long) extends SizeOf
  case class Instance(o: Object, mode: Mode) extends SizeOf
  case class Fields(o: Object, fs: Array[Field], mode: Mode) extends SizeOf
  case class Sum(os: Iterable[SizeOf]) extends SizeOf

  val Empty: SizeOf = Constant(0L)
}
