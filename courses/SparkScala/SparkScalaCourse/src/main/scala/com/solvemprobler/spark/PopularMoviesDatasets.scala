package com.solvemprobler.spark

import java.nio.charset.CodingErrorAction

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._
import org.apache.spark.sql.functions._
import scala.io.{Codec, Source}

object PopularMoviesDatasets {
  /** Load up a Map of movie IDs to movie names. */
  def loadMovieNames() : Map[Int, String] = {

    // Handle character encoding issues:
    implicit val codec = Codec("UTF-8")
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Create a Map of Ints to Strings, and populate it from u.item.
    var movieNames:Map[Int, String] = Map()

    val lines = Source.fromFile("../ml-100k/u.item").getLines()

    for (line <- lines) {
      val fields = line.split('|')
      if (fields.length > 1) {
        movieNames += (fields(0).toInt -> fields(1))
      }
    }

    return movieNames
  }

  final case class Movie(movieId: Int)

  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("PopularMoviesDatasets")
      .master("local[*]")
      .getOrCreate()

    // Create a broadcast variable of our ID -> movie name map
    val nameDict = spark.sparkContext.broadcast(loadMovieNames) // this dictionary is shared across the nodes
    import spark.implicits._
    // Load each line of the source data into an RDD
    val lines = spark.sparkContext.textFile("../ml-100k/u.data")
    val moviesDS = lines.map(x => Movie(x.split("\t")(1).toInt)).toDS

    val result = moviesDS.groupBy("movieId").count().orderBy(desc("count")).cache()
    result.show()

    val top10 = result.take(10)

    for (row <- top10) {
      println(nameDict.value(row(0).asInstanceOf[Int]) + ": " + row(1))
    }

    spark.stop()
  }
}
