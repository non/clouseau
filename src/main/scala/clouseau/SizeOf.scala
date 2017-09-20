package clouseau

import java.lang.reflect.Field
import scala.annotation.tailrec
import scala.collection.mutable

sealed abstract class SizeOf extends Product with Serializable

object SizeOf {

  case class Constant(n: Long) extends SizeOf
  case class Instance(o: Object) extends SizeOf
  case class Fields(o: Object, fs: Array[Field]) extends SizeOf
  case class Sum(os: Iterable[SizeOf]) extends SizeOf

  val empty: SizeOf = Constant(0L)

  def apply(o: Object): Long = calculate(o, mutable.Set.empty).bytes

  case class Result(bytes: Long, seen: mutable.Set[Long])

  def calculate(o: Object, seen: mutable.Set[Long]): Result = {
    val inst = Inst.instrumentation
    @tailrec def loop(queue: List[SizeOf], bytes: Long): Long =
      queue match {
        case Nil =>
          bytes
        case Instance(null) :: rest =>
          loop(rest, bytes)
        case Instance(o) :: rest =>
          val x = Identity.hash(o)
          if (seen(x)) loop(rest, bytes)
          else {
            seen += x
            val n = inst.getObjectSize(o)
            loop(Members.of(o) :: rest, bytes + n)
          }
        case Fields(o, fs) :: rest =>
          val s = Sum(fs.map(f => Instance(f.get(o))))
          loop(s :: rest, bytes)
        case Sum(xs) :: rest =>
          loop(xs.toList ::: rest, bytes)
        case Constant(n) :: rest =>
          loop(rest, bytes + n)
      }
    val bytes = loop(Instance(o) :: Nil, 0L)
    Result(bytes, seen)
  }
}
