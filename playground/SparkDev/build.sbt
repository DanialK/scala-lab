name := "SparkDev"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.2",
  "org.apache.spark" %% "spark-sql" % "2.2.2",
  "org.apache.spark" %% "spark-mllib" % "2.2.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)