package clouseau

import java.{util => u}
import org.scalacheck.{Arbitrary, Gen, Properties}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import scala.collection.{mutable, immutable}

import clouseau.Calculate.{sizeOf, staticSizeOf, fullSizeOf}

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

  property("fullSize(a) >= sizeOf(a) >= 0") = forAll { (a0: Anything) =>
    val a = a0.value
    val s0 = sizeOf(a)
    val s1 = fullSizeOf(a)
    s1 >= s0 && s0 >= 0L
  }

  property("fullSize(a) >= staticSizeOf(a) >= 0") = forAll { (a0: Anything) =>
    val a = a0.value
    val s0 = staticSizeOf(a)
    val s1 = fullSizeOf(a)
    s1 >= s0 && s0 >= 0L
  }

  // sizeOf(a) includes
  //   - instance members of a's instance members.
  //
  // staticSizeOf(a) includes
  //   - instance members of a's static members.
  //   - static members of a's static members.
  //
  // fullSize(a) includes
  //   - instance members of a's instance members.
  //   - instance members of a's static members.
  //   - static members of a's instance members.
  //   - static members of a's static members.
  //
  // therefore, the following inequality doesn't hold, since the sum
  // fails to cover 'static members of a's instance members'.

  // property("(sizeOf(a) + staticSizeOf(a)) >= fullSize(a)") = forAll { (a0: Anything) =>
  //   val a = a0.value
  //   val s0 = sizeOf(a)
  //   val s1 = staticSizeOf(a)
  //   val s2 = fullSizeOf(a)
  //   (s0 + s1) >= s2
  // }

  property("sizeOf(x :: xs) > sizeOf(xs)") =
    forAll { (x0: Anything, xs0: List[Anything]) =>
      val (x, xs) = (x0.value, xs0.map(_.value))
      sizeOf(x :: xs) > sizeOf(xs)
    }


  property("sizeOf(Option(a)) > sizeOf(a)") = forAll { (a0: Anything) =>
    val a = a0.value
    sizeOf(Option(a)) > sizeOf(a)
  }

  property("sizeOf((a, b)) > (sizeOf(a) max sizeOf(b))") = forAll { (a0: Anything, b0: Anything) =>
    val (a, b) = (a0.value, b0.value)
    sizeOf((a, b)) > (sizeOf(a) max sizeOf(b))
  }

  property("sizeOf(Array(a, b)) >= sizeOf(Array(a, a))") =
    forAll { (a0: Anything, b0: Anything) =>
      val (a, b) = (a0.value, b0.value)
      sizeOf(Array(a, b)) >= sizeOf(Array(a, a))
    }

  property("sizeOf(x :: xs) > sizeOf(xs)") =
    forAll { (x0: Anything, xs0: List[Anything]) =>
      val (x, xs) = (x0.value, xs0.map(_.value))
      sizeOf(x :: xs) > sizeOf(xs)
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

  class Prof(val name: String, val sizeOf: Int => Long)

  object Prof {
    def apply[C <: AnyRef](name: String, f: Int => C): Prof =
      new Prof(name, (n: Int) => sizeOf(f(n)))

    def range[C <: AnyRef](name: String, f: Range => C): Prof =
      new Prof(name, (n: Int) => sizeOf(f(1 to n)))
  }

  val profiles: Vector[Prof] =
    Vector(
      Prof.range("Array[Int]", _.toArray),
      // Prof.range("debox.Buffer[Int]", { r => val b = debox.Buffer.empty[Int]; b ++= r; b }),
      // Prof.range("debox.Set[Int]", { r => val s = debox.Set.empty[Int]; s ++= r; s }),
      // Prof.range("debox.Map[Int,Int]", build(debox.Map.empty[Int, Int]) { (m, n) => m(n) = n; m }),
      Prof.range("immutable.BitSet", r => immutable.BitSet(r: _*)),
      Prof.range("immutable.IntMap[Int]", r => immutable.IntMap(r.map(i => (i, i)): _*)),
      Prof.range("immutable.List[Int]", _.toList),
      Prof.range("immutable.Map[Int,Int]", _.iterator.map(i => (i, i)).toMap),
      Prof.range("immutable.Queue[Int]", r => immutable.Queue(r: _*)),
      Prof.range("immutable.Vector[Int]", _.toVector),
      Prof.range("immutable.Set[Int]", r => immutable.Set(r: _*)),
      Prof.range("immutable.SortedMap[Int,Int]", r => immutable.SortedMap(r.map(i => (i, i)): _*)),
      Prof.range("immutable.SortedSet[Int]", r => immutable.SortedSet(r: _*)),
      Prof.range("immutable.Stream[Int]", r => r.toStream),
      Prof.range("immutable.TreeMap[Int,Int]", r => immutable.TreeMap(r.map(i => (i, i)): _*)),
      Prof.range("immutable.TreeSet[Int]", r => immutable.TreeSet(r: _*)),
      Prof.range("mutable.ArrayBuffer[Int]", r => mutable.ArrayBuffer(r: _*)),
      Prof.range("mutable.BitSet", r => mutable.BitSet(r: _*)),
      Prof.range("mutable.Map[Int,Int]", r => mutable.Map(r.map(i => (i, i)): _*)),
      Prof.range("mutable.PriorityQueue[Int]", r => mutable.PriorityQueue(r: _*)),
      Prof.range("mutable.Set[Int]", r => mutable.Set(r: _*)),
      Prof.range("j.u.ArrayDeque[Int]", build(new u.ArrayDeque[Int])((d, n) => d.add(n))),
      Prof.range("j.u.ArrayList[Int]", build(new u.ArrayList[Int])((a, n) => a.add(n))),
      Prof.range("j.u.HashMap[Int]", build(new u.HashMap[Int, Int])((m, n) => m.put(n, n))),
      Prof.range("j.u.HashSet[Int]", build(new u.HashSet[Int])((s, n) => s.add(n))),
      Prof.range("j.u.LinkedList[Int]", build(new u.LinkedList[Int])((l, n) => l.add(n))),
      Prof.range("j.u.LinkedHashMap[Int]", build(new u.LinkedHashMap[Int, Int])((m, n) => m.put(n, n))),
      Prof.range("j.u.LinkedHashSet[Int]", build(new u.LinkedHashSet[Int])((s, n) => s.add(n))),
      Prof.range("j.u.PriorityQueue[Int]", build(new u.PriorityQueue[Int])((p, n) => p.add(n))),
      Prof.range("j.u.TreeMap[Int,Int]", build(new u.TreeMap[Int, Int])((s, n) => s.put(n, n))),
      Prof.range("j.u.TreeSet[Int]", build(new u.TreeSet[Int])((s, n) => s.add(n))),
      Prof.range("j.u.Vector[Int]", build(new u.Vector[Int])((v, n) => v.add(n)))
    ).sortBy(_.name)

  def build[C, U](init: => C)(f: (C, Int) => U): Range => C = { r =>
    val c = init
    r.foreach { n => f(c, n) }
    c
  }

  property("ad-hoc") = {
    type Pair[A] = (A, A)
    val p0: Pair[Int] = (1, 2)
    val p1: Pair[Integer] = (new Integer(1), new Integer(3))
    val p2: Pair[Integer] = (null, null)
    println(s"sizeOf((1, 2)) = ${sizeOf(p0)}")
    println(s"sizeOf((new Integer(1), new Integer(2))) = ${sizeOf(p1)}")
    println(s"sizeOf((null, null)) = ${sizeOf(p2)}")
    true
  }

  property("collection benchmark") = {
    import java.io._
    //val logFile: Option[String] = Some("measurements.log")
    val logFile: Option[String] = None
    val os: OutputStream = logFile match {
      case Some(path) => new FileOutputStream(new File(path))
      case None => System.out
    }
    val pw = new PrintWriter(os)
    val n = profiles.iterator.map(_.name.length).max
    val sizes = List(0, 1, 5, 10, 50, 100, 500, 1000)
    val fmt = s"%-${n}s " + sizes.map(_ => "%8s").mkString(" ")
    val headers = "COLLECTION" :: sizes.map(_.toString)
    pw.println(fmt.format(headers: _*))
    profiles.foreach { p =>
      val tokens = p.name :: sizes.map(n => p.sizeOf(n))
      pw.println(fmt.format(tokens: _*))
    }
    pw.close()
    logFile.foreach(path => println(s"wrote output to $path"))
    true
  }
}
