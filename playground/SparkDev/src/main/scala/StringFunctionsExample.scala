import org.apache.spark.sql.functions._

object StringFunctionsExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._
    val df = Seq(
      ("white t shirt"),
      ("black cat"),
      ("red short"),
      ("green tea")
    ).toDF("name")

    df.select(initcap(col("name"))).show()

    df.select(
      ltrim(lit("    HELLO    ")).as("ltrim"),
      rtrim(lit("    HELLO    ")).as("rtrim"),
      trim(lit("    HELLO    ")).as("trim"),
      lpad(lit("HELLO"), 3, " ").as("lp"),
      rpad(lit("HELLO"), 10, " ").as("rp")).show(2)


    val simpleColors = Seq("black", "white", "red", "green", "blue")
    val selectedColumns = simpleColors.map(color => {
      col("name").contains(color.toUpperCase).alias(s"is_$color")
    }):+expr("*") // could also append this value
    df.select(selectedColumns:_*)
      .where(col("is_white").or(col("is_red")))
      .select("name")
      .show()

    spark.stop()
  }
}
