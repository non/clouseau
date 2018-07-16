package clouseau
package bench

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import scala.util.Random

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class CalculateBenchmark {

  implicit class OneOf[A](xs: Vector[A]) {
    def oneOf(): A = xs(Random.nextInt(xs.length))
  }

  var subject: Object = null

  @Setup
  def setup(): Unit = {

    val N = 20

    // warm up
    Calculate.sizeOf(List(1,2,3))

    def alloc0(): Vector[Long] =
      (1 to N).map(_ => Random.nextLong).toVector

    val vects0: Vector[Vector[Long]] =
      (1 to N).map(_ => alloc0()).toVector

    def alloc1(): Map[Int, Vector[Long]] =
      (1 to N).map(_ => (Random.nextInt, vects0.oneOf())).toMap

    val maps1: Vector[Map[Int, Vector[Long]]] =
      (1 to N).map(_ => alloc1()).toVector

    def alloc2(): Map[Int, Map[Int, Vector[Long]]] =
      (1 to N).map(_ => (Random.nextInt, maps1.oneOf())).toMap

    val maps2: Vector[Map[Int, Map[Int, Vector[Long]]]] =
      (1 to N).map(_ => alloc2()).toVector

    subject = maps2
  }

  @Benchmark
  def calculateSizeOf(): Long =
    Calculate.sizeOf(subject)

  // @Benchmark
  // def calculateStaticSizeOf(): Long =
  //   Calculate.staticSizeOf(subject)
  //
  // @Benchmark
  // def calculateFullSizeOf(): Long =
  //   Calculate.fullSizeOf(subject)
}
