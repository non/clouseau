package clouseau

import java.lang.System

object Identity {

  /**
   * Best-effort attempt to hash distinct (but equal/identical)
   * objects to different Long values.
   *
   * System.identityHashCode is controlled by the JVM (and likely
   * based on internal IDs used for GC, or memory layout). o.hashCode
   * is controlled by o's class. o.getClass.hashCode distinguishes
   * objects that happen to collide but have different classes.
   *
   * Two instances of the same class that happen to collide by their
   * class' hash code should still have only a 1-in-4 billion chance
   * of colliding. All other collisions should be even less probable.
   */
  def hash(o: Object): Long = {
    val x = System.identityHashCode(o) & 0xffffffffL
    val y = o.getClass.hashCode & 0xffffffffL
    try {
      val z = o.hashCode & 0xffffffffL
      x ^ (y << 16) ^ (z << 32)
    } catch { case (e: Exception) =>
      x ^ (y << 32)
    }
  }

  def hashClass(c: Class[_]): Long = hash(c)
}
