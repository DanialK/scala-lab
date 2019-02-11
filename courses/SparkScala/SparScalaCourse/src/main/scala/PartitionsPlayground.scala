import org.apache.log4j._
import org.apache.spark.sql._


object PartitionsPlayground {
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("PartitionsPlayground")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val rdd = spark.sparkContext.parallelize(Seq(
      (1, 1),
      (1, 2),
      (2, 3),
      (2, 4),
      (3, 5),
      (3, 6),
      (3, 7)
    )).toDF()

    println(rdd.rdd.partitions.length)


    println(spark.range(2, 100000, 2).rdd.partitions.length)
//    println(rdd.countByKey())

//    val a = rdd.mapPartitions(part => part.map(x => 1)).sum()
//    println(a)
  }
}
