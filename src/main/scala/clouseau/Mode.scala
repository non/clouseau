package clouseau

sealed abstract class Mode {
  def includeClass: Boolean =
    this != Mode.JustStatic
  def includeStatic: Boolean =
    this != Mode.JustClass
  def childMode: Mode =
    if (this == Mode.JustClass) this else Mode.ClassAndStatic
}

object Mode {
  case object JustClass extends Mode
  case object JustStatic extends Mode
  case object ClassAndStatic extends Mode
}
