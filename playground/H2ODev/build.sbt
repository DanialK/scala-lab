name := "H2ODev"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.2.2" % "provided",
  "ai.h2o" % "sparkling-water-core_2.11" % "2.2.22" % "provided",
  "ai.h2o" % "sparkling-water-ml" % "2.2.22" % "provided"
)