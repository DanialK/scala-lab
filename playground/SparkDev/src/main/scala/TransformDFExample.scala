import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

object TransformDFExample extends SparkSessionWrapper {

  def withUserId(df: DataFrame): DataFrame = {
    df.withColumn(
      "user_id",
      md5(concat_ws(",", col("first_name"), col("last_name")))
    )
  }

  def withFullName(df: DataFrame): DataFrame = {
    df.withColumn(
      "full_name",
      concat(col("first_name"), lit(" "), col("last_name"))
    )
  }

  def withHasVowel(df: DataFrame): DataFrame = {
    val conditions = List("a", "e", "i", "o", "u")
      .map(vowel => when(col("full_name").contains(vowel), true))
      .foldRight(lit(false))((chain, condition) => chain.otherwise(condition))

    df.withColumn(
      "has_vowel",
      conditions
//      when(col("full_name").contains("a"), true).otherwise(
//        when(col("full_name").contains("e"), true).otherwise(
//          when(col("full_name").contains("i"), true).otherwise(
//            when(col("full_name").contains("o"), true).otherwise(
//              when(col("full_name").contains("u"), true).otherwise(
//                false
//              )
//            )
//          )
//        )
//      )
    )
  }

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val sourceDF = Seq(
      ("Danial", "Khosravi"),
      ("John", "Doe"),
      ("Roger", "Waters"),
      ("Dnl", "Khsrv"),
      ("Jhn", "D"),
      ("Rgr", "Wtrs"),
      ("a", "b"),
      ("e", "f"),
      ("i", "j"),
      ("o", "p"),
      ("u", "v")
    ).toDF("first_name", "last_name")

    val resultDF = sourceDF
      .transform(withUserId)
      .transform(withFullName)
      .transform(withHasVowel)

    resultDF.show()

    spark.stop()
  }
}
