name := "fp-to-the-max"

version := "0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-language:higherKinds"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalaz" %% "scalaz-core" % "7.2.26"
)