import org.apache.spark.sql.functions._

object RepartitionExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val df1 = Seq(
      (1, "A"),
      (2, "B"),
      (3, "C"),
      (4, "D"),
      (5, "E"),
      (6, "F")
    ).toDF("id", "char")

    val df2 = df1.withColumn("id", $"id" + lit(6))

    val data = df1.union(df2)

    println(data.rdd.getNumPartitions) // 8

    println(data.repartition(6).rdd.getNumPartitions) // 6

    println(data.repartition($"char").rdd.getNumPartitions) // 200

    // coalesce avoids a full shuffle and try to combine partitions
    println(data
      .repartition($"char")
      .coalesce(2).rdd.getNumPartitions) // 2

    spark.stop()
  }
}
