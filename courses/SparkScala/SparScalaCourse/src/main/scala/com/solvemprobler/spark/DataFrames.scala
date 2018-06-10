package com.solvemprobler.spark
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._

object DataFrames {

  case class Person(Id: Int, name: String, age: Int, numFriends: Int)

  def parseLine(line: String): Person = {
    val values = line.split(",")
    return Person(values(0).toInt, values(1), values(2).toInt, values(3).toInt)
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("DataFrames")
      .master("local[*]")
//      .config("spark.sql.warehouse.dir", "file:///C:/temp")
      .getOrCreate()

    import spark.implicits._
    val lines = spark.sparkContext.textFile("../fakefriends.csv")
    val people = lines.map(parseLine).toDS().cache()

    people.printSchema()

    people.select("name").show()

    people.filter(people("age") < 21).show()
    people.filter($"age" < 20).show()
    people.groupBy("age").count().show()
    people.select($"name", $"age" + 10).show()

    spark.stop()
  }

}
