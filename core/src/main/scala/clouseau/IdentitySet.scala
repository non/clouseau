package clouseau

import java.util.IdentityHashMap

/**
 * Provides a set-like interface for IdentityHashMap[Object, Unit].
 */
class IdentitySet(members: IdentityHashMap[Object, Unit]) {
  def apply(o: Object): Boolean = members.containsKey(o)
  def +=(o: Object): Unit = members.put(o, ())
}

object IdentitySet {
  def empty: IdentitySet = new IdentitySet(new IdentityHashMap(1024))
}

