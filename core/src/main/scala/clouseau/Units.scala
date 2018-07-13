package clouseau

object Units {
  def bytes(bytes: Long): String = s"${bytes}B"

  val si: List[String] = List("B", "K", "M", "G", "T", "P", "E", "Z", "Y")

  def approx(bytes: Long): String = {
    def loop(value: Double, units: List[String]): String =
      if (value < 1024.0) "%.3g%s".format(value, units.head)
      else loop(value / 1024.0, units.tail)
    loop(bytes.toDouble, si)
  }
}
