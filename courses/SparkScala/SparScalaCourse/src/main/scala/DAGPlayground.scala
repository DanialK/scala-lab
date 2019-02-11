import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.log4j._


object DAGPlayground {
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("PartitionsPlayground")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

//    val rdd = spark.sparkContext.parallelize(Seq(
//      (1, 1),
//      (1, 2),
//      (2, 3),
//      (2, 4),
//      (3, 5),
//      (3, 6),
//      (3, 7)
//    ))
//
//    println(rdd.countByKey())

    val df1 = spark.sparkContext.parallelize(Seq(
      (1, 1),
      (1, 2),
      (2, 3),
      (2, 4),
      (3, 5),
      (3, 6),
      (3, 7)
    )).toDF("a", "b")

    val df2 = spark.sparkContext.parallelize(Seq(
      (1, 1),
      (1, 2),
      (2, 3),
      (2, 4),
      (3, 5),
      (3, 6),
      (3, 7)
    )).toDF("a", "c")


    val stage1 = df1.join(df2, "a").cache()

    stage1.show()

    val stage2 = stage1.repartition(7).cache()

    stage2.show()

    val stage3 = stage2.withColumn("d", $"b" + $"c")

    stage3.show()
//
//    val dfv2 = df.withColumn("c", $"a" + lit(10))
//
//    dfv2.show()

//    val dfv3 = df.groupBy($"a").count().cache()

//    dfv3.show()

    scala.io.StdIn.readLine()
  }
}
