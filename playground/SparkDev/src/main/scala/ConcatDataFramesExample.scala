object ConcatDataFramesExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val df1 = Seq(
      (1, "A"),
      (2, "B"),
      (3, "C")
    ).toDF("id", "char")

    val df2 = Seq(
      (4, "D"),
      (5, "E"),
      (6, "F")
    ).toDF("id", "char")

    df1.union(df2).show()

    spark.stop()
  }
}
