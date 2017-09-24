package clouseau

import java.lang.{reflect => r}
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier.isStatic
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Members {

  def of(o: Object, seen: mutable.Set[Long], m: Mode): SizeOf = {
    val x = if (m.includeClass) forInstance(o, seen, m) else SizeOf.Empty
    val y = if (m.includeStatic) forStatic(o, seen) else SizeOf.Empty
    SizeOf.Sum(x :: y :: Nil)
  }

  def forInstance(o: Object, seen: mutable.Set[Long], m: Mode): SizeOf = {
    val c = o.getClass
    if (c.isArray) forArray(o, c, seen, m) else forClass(o, c, seen, m)
  }

  private def forArray(o: Object, c: Class[_], seen: mutable.Set[Long], m0: Mode): SizeOf = {
    val m1 = m0.childMode
    if (c.getComponentType.isPrimitive) SizeOf.Empty
    else SizeOf.Sum((0 until r.Array.getLength(o)).iterator
      .map(i => r.Array.get(o, i))
      .filter(_ != null)
      .map(SizeOf.Instance(_, m1))
      .toVector)
  }

  private def forClass(o: Object, c: Class[_], seen: mutable.Set[Long], m: Mode): SizeOf = {
    @tailrec def loop(c1: Class[_], buf: ArrayBuffer[Field]): Array[Field] = {
      c1.getDeclaredFields.iterator
        .filter(f => !isStatic(f.getModifiers) && !f.getType.isPrimitive)
        .foreach(f => buf += f)
      val s: Class[_] = c1.getSuperclass
      if (s != null) loop(s, buf) else buf.toArray
    }

    val fields = loop(o.getClass, ArrayBuffer.empty)
    if (fields.isEmpty) SizeOf.Empty
    else {
      AccessibleObject.setAccessible(fields.asInstanceOf[Array[AccessibleObject]], true)
      val (prims, objects) = fields.partition(_.getType.isPrimitive)
      val a = SizeOf.Constant(prims.iterator.map(SizeOf.primitive).sum)
      val b = SizeOf.Fields(o, objects, m.childMode)
      SizeOf.Sum(a :: b :: Nil)
    }
  }

  def forStatic(o: Object, seen: mutable.Set[Long]): SizeOf = {
    @tailrec def loop(c1: Class[_], buf: ArrayBuffer[Field]): Array[Field] = {
      val x = Identity.hashClass(c1)
      if (seen(x)) buf.toArray
      else {
        seen += x
        c1.getDeclaredFields.iterator
          .filter(f => isStatic(f.getModifiers))
          .foreach(f => buf += f)
        val s: Class[_] = c1.getSuperclass
        if (s == null) buf.toArray else loop(s, buf)
      }
    }

    val c0 = o.getClass
    val fields = loop(c0, ArrayBuffer.empty)
    if (fields.isEmpty) {
      SizeOf.Empty
    } else {
      AccessibleObject.setAccessible(fields.asInstanceOf[Array[AccessibleObject]], true)
      val (prims, objects) = fields.partition(_.getType.isPrimitive)
      val a = SizeOf.Constant(prims.iterator.map(SizeOf.primitive).sum)
      val b = SizeOf.Fields(o, objects, Mode.ClassAndStatic)
      SizeOf.Sum(a :: b :: Nil)
    }
  }
}
