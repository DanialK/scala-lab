import org.apache.spark.sql.functions._

object ArrayFunctionslExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val df = Seq(
      (1, "A"),
      (2, "A"),
      (3, "A"),
      (4, null),
      (5, "B"),
      (6, "B")
    ).toDF("id", "char")

    df.where(col("char") === null).show()

    df.where(col("char") === lit(null)).show()

    df.where(col("char").eqNullSafe(null)).show()

    df.select($"id", pow($"id", 2) + 5 as "yp").show()

    df.na.drop().show()

    df.na.drop("any").show()

    df.na.drop("all").show()

    spark.stop()
  }
}
