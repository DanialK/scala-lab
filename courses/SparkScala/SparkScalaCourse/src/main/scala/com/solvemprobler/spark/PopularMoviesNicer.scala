package com.solvemprobler.spark

import java.nio.charset.CodingErrorAction

import org.apache.log4j._
import org.apache.spark._

import scala.io.{Codec, Source}

object PopularMoviesNicer {
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

  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "PopularMoviesNicer")

    // Create a broadcast variable of our ID -> movie name map
    var nameDict = sc.broadcast(loadMovieNames) // this dictionary is shared across the nodes

    // Load each line of the source data into an RDD
    val lines = sc.textFile("../ml-100k/u.data")
    val movies = lines.map(x => (x.split("\t")(1).toInt, 1))
    val moviesSorted = movies.reduceByKey(_+_).sortBy(x => x._2)
    val moviesSortedWithName = moviesSorted.map(x => (nameDict.value(x._1), x._2))
    val result = moviesSortedWithName.collect()
    result.foreach(println)
  }
}
