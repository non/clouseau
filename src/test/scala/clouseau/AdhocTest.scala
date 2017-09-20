package clouseau

import org.scalacheck.Properties

object AdHocTest extends Properties("AdHoc") {

  property("ad-hoc") = {
    val o = List(1)
    println(Calculate.sizeOf(o))
    println(Calculate.staticSizeOf(o))
    println(Calculate.fullSizeOf(o))
    true
  }
}
