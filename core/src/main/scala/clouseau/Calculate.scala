package clouseau

import scala.annotation.tailrec

object Calculate {

  import SizeOf._

  /**
   * Return the size of the given object and its members in bytes.
   *
   * The total here does not include static members from the object's
   * parent classes.
   *
   * The size of an object can change over time, and will not include
   * native memory which is not accessible. For example, a call to
   * ByteBuffer.allocate(4096) will look significantly larger than a
   * similar call to ByteBuffer.allocateDirect(4096), because the
   * latter allocates native memory which is invisible to us.
   *
   * The size of each member is computed using the above logic.
   */
  def sizeOf(o: Object): Long =
    calculate(o, IdentitySet.empty, Mode.JustClass).bytes

  /**
   * Return the size of the given object's static members in bytes.
   *
   * An object's static members are considered to be the static
   * members of its parent classes.
   *
   * The size of each static member is computed using the same logic
   * as sizeOf().
   */
  def staticSizeOf(o: Object): Long =
    calculate(o, IdentitySet.empty, Mode.JustStatic).bytes

  /**
   * Return the size of the given object along with its members and
   * static members in bytes.
   *
   * This method gives you the "total footprint" of an object: all the
   * objects in memory which it can possibly reach (either by
   * reference or statically). The size returned is not obviously
   * related to the sizes returned by sizeOf() and staticSizeOf(),
   * except that it is guaranteed to be larger.
   *
   * The size of each member (static and non-static) is computed using the above logic.
   */
  def fullSizeOf(o: Object): Long =
    calculate(o, IdentitySet.empty, Mode.ClassAndStatic).bytes

  /**
   * Represents a running total of bytes along with a set of objects
   * already seen and accounted for.
   */
  case class Result(bytes: Long, seen: IdentitySet)

  /**
   * Lower-level method to calculate object sizes.
   *
   * This method powers sizeOf(), staticSizeOf(), and fullSizeOf(),
   * and can also be used to compute more complex size relationships
   * (e.g. the size of A excluding members of B), as well as how sizes
   * change over time.
   */
  def calculate(
    o: Object,
    seen: IdentitySet = IdentitySet.empty,
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
          if (seen(o)) loop(rest, bytes)
          else {
            seen += o
            val n = if (mode.includeClass) inst.getObjectSize(o) - enumCost(o) else 0L
            loop(Members.of(o, seen, mode) :: rest, bytes + n)
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
