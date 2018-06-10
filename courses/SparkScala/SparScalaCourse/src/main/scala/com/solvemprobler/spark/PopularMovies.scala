package com.solvemprobler.spark

import org.apache.log4j._
import org.apache.spark._

object PopularMovies {
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "PopularMovies")

    // Load each line of the source data into an RDD
    val lines = sc.textFile("../ml-100k/u.data")
    val movies = lines.map(x => (x.split("\t")(1).toInt, 1))
    val result = movies.reduceByKey(_+_).sortBy(x => x._2).collect()
    result.foreach(println)
  }
}
