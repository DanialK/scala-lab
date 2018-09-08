import org.apache.spark.sql.functions._

object JoinBroadcastExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val sourceDF = Seq(
      ("britney spears", "Mississippi"),
      ("romeo santos", "New York"),
      ("miley cyrus", "Tennessee"),
      ("random dude", null),
      (null, "Dubai")
    ).toDF("name", "birth_state")

    val stateMappingsDF = spark
      .read
      .option("header", "true")
      .option("charset", "UTF8")
      .csv("data/state_mappings.csv")

    val resultDF = sourceDF.join(
      broadcast(stateMappingsDF),
      sourceDF("birth_state") === stateMappingsDF("state_name"),
      "left_outer"
    ).drop(stateMappingsDF("state_name"))

    resultDF.show()

    spark.stop()
  }
}
