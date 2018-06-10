package com.solvemprobler.spark
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._

object SparkSQL {

  case class Person(Id: Int, name: String, age: Int, numFriends: Int)

  def parseLine(line: String): Person = {
    val values = line.split(",")
    return Person(values(0).toInt, values(1), values(2).toInt, values(3).toInt)
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("SparkSQL")
      .master("local[*]")
//      .config("spark.sql.warehouse.dir", "file:///C:/temp")
      .getOrCreate()

    import spark.implicits._
    val lines = spark.sparkContext.textFile("../fakefriends.csv")
    val people = lines.map(parseLine).toDS().cache()

    people.printSchema()

    people.select("name").show()

    people.createOrReplaceTempView("people")

    // SQL can be run over DataFrames that have been registered as a table
    val teenagers = spark.sql("SELECT * FROM people WHERE age >= 13 AND age <= 19")

    val results = teenagers.collect()

    results.foreach(println)
    spark.stop()
  }

}
