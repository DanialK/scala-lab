package com.solvemprobler.spark

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext

object MostPopularSuperhero {
  def countCoOccurrences(line: String) = {
    val elements = line.split("\\s+")
    (elements(0).toInt, elements.length - 1)
  }

  // Function to extract hero ID -> hero name tuples (or None in case of failure)
  def parseNames(line: String) : Option[(Int, String)] = {
    val fields = line.split('\"')
    if (fields.length > 1) {
      return Some(fields(0).trim().toInt, fields(1))
    } else {
      return None // flatMap will just discard None results, and extract data from Some results.
    }
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val sc = new SparkContext("local[*]", "MostPopularSuperhero")
    val names = sc.textFile("../Marvel-names.txt")
    val namesRdd = names.flatMap(parseNames)

    val lines = sc.textFile("../Marvel-graph.txt")
    val pairings = lines.map(countCoOccurrences)
    // Combine entries that span more than one line
    val totalFriendsByCharacter = pairings.reduceByKey((x, y) => x + y)
//    val flipped = totalFriendsByCharacter.map( x => (x._2, x._1) )
//    val mostPopular = flipped.max()
    val mostPopular = totalFriendsByCharacter.sortBy(x => x._2, ascending = false).first()
    val mostPopularName = namesRdd.lookup(mostPopular._1)(0)
    println(s"$mostPopularName is the most popular superhero with ${mostPopular._2} co-appearances.")

  }
}
