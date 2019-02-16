name := "SparkStreaming"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.2" % Provided,
  "org.apache.spark" %% "spark-sql" % "2.2.2" % Provided,
  "org.apache.spark" %% "spark-streaming" % "2.2.2" % Provided,
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.2.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
  case PathList("javax", "inject", xs@_*) => MergeStrategy.last
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}


mainClass in (Compile, run) := Some("com.solvemprobler.sparkstreaming.StreamingApp")

mainClass in (Compile, packageBin) := Some("com.solvemprobler.sparkstreaming.StreamingApp")