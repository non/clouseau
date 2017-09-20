package clouseau

import scala.annotation.tailrec
import scala.collection.mutable

object Calculate {

  import SizeOf._

  /**
   */
  def sizeOf(o: Object): Long =
    calculate(o, mutable.Set.empty, Mode.JustClass).bytes

  /**
   */
  def staticSizeOf(o: Object): Long =
    calculate(o, mutable.Set.empty, Mode.JustStatic).bytes

  /**
   */
  def fullSizeOf(o: Object): Long =
    calculate(o, mutable.Set.empty, Mode.ClassAndStatic).bytes

  case class Result(bytes: Long, seen: mutable.Set[Long])

  def calculate(
    o: Object,
    seen: mutable.Set[Long] = mutable.Set.empty,
    mode: Mode = Mode.JustClass
  ): Result = {
    val inst = Inst.instrumentation

    val ec = classOf[Enum]
    val es = inst.getObjectSize(Enum.ENTRY)

    // this is a static cost
    def enumCost(o: Object): Long =
      if (ec.isAssignableFrom(o.getClass)) es else 0L

    @tailrec def loop(queue: List[SizeOf], bytes: Long): Long =
      queue match {
        case Nil =>
          bytes
        case Instance(null, _) :: rest =>
          loop(rest, bytes)
        case Instance(o, mode) :: rest =>
          val x = Identity.hash(o)
          if (seen(x)) loop(rest, bytes)
          else {
            seen += x
            val n = if (mode.includeClass) inst.getObjectSize(o) - enumCost(o) else 0L
            loop(Members.of(o, mode) :: rest, bytes + n)
          }
        case Fields(o, fs, mode) :: rest =>
          loop(Sum(fs.map(f => Instance(f.get(o), mode))) :: rest, bytes)
        case Sum(xs) :: rest =>
          loop(xs.toList ::: rest, bytes)
        case Constant(n) :: rest =>
          loop(rest, bytes + n)
      }
    Result(loop(Instance(o, mode) :: Nil, 0L), seen)
  }

}
