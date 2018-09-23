
object PipeRDDExample extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val myCollection = "Spark The Definitive Guide : Big Data Processing Made Simple"
      .split(" ")

    val words = spark.sparkContext.parallelize(myCollection, 5)

    words.setName("myWords")

    val result = words.pipe("wc -l").collect()

    result.foreach(println)

    spark.stop()
  }
}
