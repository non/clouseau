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
}
