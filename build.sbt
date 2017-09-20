name := "clouseau"

scalaVersion := "2.12.3"

scalacOptions ++=
  "-feature" ::
  Nil

libraryDependencies +=
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"

javaOptions += "-javaagent:target/scala-2.12/clouseau_2.12-0.1-SNAPSHOT.jar"

fork := true

packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes("Premain-Class" -> "clouseau.Inst")
