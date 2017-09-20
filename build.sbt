name := "clouseau"

//scalaVersion := "2.12.3"
scalaVersion := "2.11.11"

scalacOptions ++=
  "-feature" ::
  Nil

libraryDependencies ++=
  "org.spire-math" %% "debox" % "0.7.3" % "test" ::
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test" ::
  Nil

// TODO: test should depend on package
// TODO: jar location is hardcoded right now.

javaOptions += "-javaagent:target/scala-2.12/clouseau_2.12-0.1-SNAPSHOT.jar"

fork := true

packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes("Premain-Class" -> "clouseau.Inst")
