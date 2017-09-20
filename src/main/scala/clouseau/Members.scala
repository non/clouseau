package clouseau

import java.lang.{reflect => r}
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

object Members {

  def of(o: Object): SizeOf = {
    import SizeOf._
    val c = o.getClass
    if (c.isArray) ofArray(o, c) else ofClass(o, c)
  }

  private def ofArray(o: Object, c: Class[_]): SizeOf =
    if (c.getComponentType.isPrimitive) SizeOf.empty
    else SizeOf.Sum((0 until r.Array.getLength(o)).iterator
      .map(i => r.Array.get(o, i))
      .filter(_ != null)
      .map(SizeOf.Instance)
      .toVector)

  private def ofClass(o: Object, c: Class[_]): SizeOf = {
    @tailrec def findFields(c: Class[_], buf: ArrayBuffer[Field]): Array[Field] = {
      c.getDeclaredFields.iterator
        .filter(f => !r.Modifier.isStatic(f.getModifiers))
        .filter(f => !f.getType.isPrimitive)
        .foreach(f => buf += f)
      val s: Class[_] = c.getSuperclass
      if (s != null) findFields(s, buf) else buf.toArray
    }
    val fields = findFields(o.getClass, ArrayBuffer.empty)
    AccessibleObject.setAccessible(fields.asInstanceOf[Array[AccessibleObject]], true)
    SizeOf.Fields(o, fields)
  }
}
