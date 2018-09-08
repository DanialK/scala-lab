name := "SparkScalaCourse"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.2",
  "org.apache.spark" %% "spark-sql" % "2.2.2",
  "org.apache.spark" %% "spark-mllib" % "2.2.2",
  "org.apache.spark" %% "spark-streaming" % "2.2.2",
  "org.twitter4j" % "twitter4j-stream" % "4.0.7",
  "org.apache.spark" %% "spark-streaming-twitter" % "1.6.3"
)