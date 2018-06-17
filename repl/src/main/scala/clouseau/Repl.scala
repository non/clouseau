package clouseau

import scala.tools.nsc.MainGenericRunner

object Repl {
  def main(args: Array[String]): Unit = {
    val props = System.getProperties()
    props.setProperty("scala.usejavacp", "true")
    MainGenericRunner.main(args)
  }
}
