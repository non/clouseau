package clouseau

import org.scalacheck.{Arbitrary, Gen, Properties}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll

object SizeOfTest extends Properties("SizeOf") {

  case class Anything(value: AnyRef)

  implicit val anything: Arbitrary[Anything] =
    Arbitrary(Gen.oneOf(
      Gen.const(Anything(null)),
      arbitrary[Boolean].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Byte].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Char].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Short].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Int].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Long].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Float].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[Double].map(x => Anything(x.asInstanceOf[AnyRef])),
      arbitrary[String].map(Anything(_)),
      arbitrary[List[Int]].map(Anything(_)),
      arbitrary[Vector[String]].map(Anything(_)),
      arbitrary[Array[Byte]].map(Anything(_)),
      arbitrary[Set[Long]].map(Anything(_)),
      arbitrary[Map[Int, Boolean]].map(Anything(_)),
      arbitrary[Int => Int].map(Anything(_))))

  property("SizeOf(a) >= 0") = forAll { (a0: Anything) =>
    val a = a0.value
    SizeOf(a) >= 0L
  }

  property("SizeOf(Option(a)) > SizeOf(a)") = forAll { (a0: Anything) =>
    val a = a0.value
    SizeOf(Option(a)) > SizeOf(a)
  }

  property("SizeOf((a, b)) > (SizeOf(a) max SizeOf(b))") = forAll { (a0: Anything, b0: Anything) =>
    val (a, b) = (a0.value, b0.value)
    SizeOf((a, b)) > (SizeOf(a) max SizeOf(b))
  }

  property("SizeOf(Array(a, b)) >= SizeOf(Array(a, a))") =
    forAll { (a0: Anything, b0: Anything) =>
      val (a, b) = (a0.value, b0.value)
      SizeOf(Array(a, b)) >= SizeOf(Array(a, a))
    }

  property("SizeOf(x :: xs) > SizeOf(xs)") =
    forAll { (x0: Anything, xs0: List[Anything]) =>
      val (x, xs) = (x0.value, xs0.map(_.value))
      SizeOf(x :: xs) > SizeOf(xs)
    }

  property("(Identity.hash(x) == Identity.hash(y)) == (x eq y)") =
    forAll { (x0: Anything, y0: Anything) =>
      val (x, y) = (x0.value, y0.value)
      if (x != null && y != null) {
        (Identity.hash(x) == Identity.hash(y)) == (x eq y)
      } else {
        true
      }
    }
}
