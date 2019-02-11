import org.apache.spark.sql.functions._

object DateFunctionsExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {

    import spark.implicits._

    val dateDF = spark.range(10)
      .withColumn("today", current_date())
      .withColumn("now", current_timestamp())

    dateDF
      .select(
        date_sub(col("today"), 5),
        date_add(col("today"), 5)
      ).show()

    dateDF
      .withColumn("week_ago", date_sub(col("today"), 7))
      .select(datediff(col("week_ago"), col("today")))
      .show(1)

    dateDF
      .select(
        to_date(lit("2016-01-01")).alias("start"),
        to_date(lit("2017-05-22")).alias("end")
      )
      .select(months_between(col("start"), col("end")))
      .show(1)

    spark
      .range(5)
      .withColumn("date", lit("2017-01-01"))
      .select(to_date(col("date")))
      .show(1)

    spark.stop()
  }
}
