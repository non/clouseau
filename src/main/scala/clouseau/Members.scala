package clouseau

import java.lang.{reflect => r}
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

import r.Modifier.isStatic

object Members {

  def of(o: Object, mode: Mode): SizeOf = {
    import SizeOf._
    val c = o.getClass
    if (c.isArray) ofArray(o, c, mode) else ofClass(o, c, mode)
  }

  private def ofArray(o: Object, c: Class[_], mode: Mode): SizeOf = {
    val m = mode.childMode
    if (c.getComponentType.isPrimitive) SizeOf.Empty
    else SizeOf.Sum((0 until r.Array.getLength(o)).iterator
      .map(i => r.Array.get(o, i))
      .filter(_ != null)
      .map(SizeOf.Instance(_, m))
      .toVector)
  }

  private def ofClass(o: Object, c: Class[_], mode: Mode): SizeOf = {
    import Mode._
    val m = mode.childMode
    val fields = mode match {
      case JustClass => findFields(o)(f => !isStatic(f.getModifiers) && !f.getType.isPrimitive)
      case JustStatic => findFields(o)(f => isStatic(f.getModifiers))
      case ClassAndStatic => findFields(o)(f => isStatic(f.getModifiers) || !f.getType.isPrimitive)
    }

    val (prims, objects) = fields.partition(_.getType.isPrimitive)
    val a = SizeOf.Constant(prims.iterator.map(primitiveSize).sum)
    val b = SizeOf.Fields(o, objects, m)
    SizeOf.Sum(a :: b :: Nil)
  }

  def findFields(o: Object)(p: Field => Boolean): Array[Field] = {
    @tailrec def loop(c: Class[_], buf: ArrayBuffer[Field]): Array[Field] = {
      c.getDeclaredFields.iterator.filter(p).foreach(f => buf += f)
      val s: Class[_] = c.getSuperclass
      if (s != null) loop(s, buf) else buf.toArray
    }
    val fields = loop(o.getClass, ArrayBuffer.empty)
    AccessibleObject.setAccessible(fields.asInstanceOf[Array[AccessibleObject]], true)
    //println(s"  fields($o): ${fields.toList}")
    fields
  }

  def primitiveSize(f: Field): Long =
    f.getType match {
      case java.lang.Long.TYPE => 8L
      case java.lang.Double.TYPE => 8L
      case java.lang.Integer.TYPE => 4L
      case java.lang.Float.TYPE => 4L
      case java.lang.Short.TYPE => 2L
      case java.lang.Character.TYPE => 2L
      case java.lang.Byte.TYPE => 1L
      case java.lang.Boolean.TYPE => 1L
      case java.lang.Void.TYPE => 0L
      case t => sys.error(s"$t")
    }
}
