package clouseau

import org.scalacheck.Properties

final class Blah

object AdHocTest extends Properties("AdHoc") {

  // property("ad-hoc") = {
  //   val o = List(1)
  //   println(Calculate.sizeOf(o))
  //   println(Calculate.staticSizeOf(o))
  //   println(Calculate.fullSizeOf(o))
  //   true
  // }

  property("static sizes comparison") = {
    val x = "this is a sentence"
    val o = List(x, x)
    println("sizeOf(new Blah) = %d" format Calculate.sizeOf(new Blah))
    println("")
    println("sizeOf(x) = %d" format Calculate.sizeOf(x))
    println("staticSizeOf(x) = %d" format Calculate.staticSizeOf(x))
    println("fullSizeOf(x) = %d" format Calculate.fullSizeOf(x))
    println("")
    println("sizeOf(Nil) = %d" format Calculate.sizeOf(Nil))
    println("staticSizeOf(Nil) = %d" format Calculate.staticSizeOf(Nil))
    println("fullSizeOf(Nil) = %d" format Calculate.fullSizeOf(Nil))
    println("")
    println("sizeOf(List(x)) = %d" format Calculate.sizeOf(List(x)))
    println("staticSizeOf(List(x)) = %d" format Calculate.staticSizeOf(List(x)))
    println("fullSizeOf(List(x)) = %d" format Calculate.fullSizeOf(List(x)))
    println("")
    println("sizeOf(o) = %d" format Calculate.sizeOf(o))
    println("staticSizeOf(o) = %d" format Calculate.staticSizeOf(o))
    println("fullSizeOf(o) = %d" format Calculate.fullSizeOf(o))
    true
  }

  property("readme") = {
    import clouseau.Mode.JustClass
    import clouseau.Calculate.{calculate, sizeOf}

    val s = IdentitySet.empty

    val m0 = (1 to 100).iterator.map(i => (i, i.toString)).toMap
    val bytes0 = calculate(m0, s, JustClass).bytes //

    val m1 = m0.updated(99, "ninety-nine")
    val bytes1 = calculate(m1, s, JustClass).bytes

    val m2 = m1.updated(1, "one")
    val bytes2 = calculate(m2, s, JustClass).bytes

    println((bytes0, sizeOf(m0)))
    println((bytes1, sizeOf(m1)))
    println((bytes2, sizeOf(m2)))
    true
  }
}
