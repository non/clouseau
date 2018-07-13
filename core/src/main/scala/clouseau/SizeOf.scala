package clouseau

import java.lang.reflect.Field

sealed abstract class SizeOf extends Product with Serializable

object SizeOf {

  case class Constant(n: Long) extends SizeOf
  case class Instance(o: Object, mode: Mode) extends SizeOf
  case class Fields(o: Object, fs: Array[Field], mode: Mode) extends SizeOf
  case class Sum(os: Iterable[SizeOf]) extends SizeOf

  val Empty: SizeOf = Constant(0L)

  def primitive(f: Field): Long =
    f.getType match {
      case java.lang.Long.TYPE => 8L
      case java.lang.Double.TYPE => 8L
      case java.lang.Integer.TYPE => 4L
      case java.lang.Float.TYPE => 4L
      case java.lang.Short.TYPE => 2L
      case java.lang.Character.TYPE => 2L
      case java.lang.Byte.TYPE => 1L
      case java.lang.Boolean.TYPE => 1L //FIXME?
      case java.lang.Void.TYPE => 0L
      case t => sys.error(s"$t")
    }
}
