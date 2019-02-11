name := "kleisli"

version := "0.1"

scalaVersion := "2.11.11"


scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.5.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.1.0"
