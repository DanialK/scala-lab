package com.solvemprobler.spark

import org.apache.log4j._
import org.apache.spark._

/** Count up how many of each word appears in a book as simply as possible. */
object WordCountSorted {

  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "WordCountSorted") // doesn't work

    // Read each line of my book into an RDD
    val input = sc.textFile("../book.txt")

    // Split into words separated by a space character
    val words = input
      .flatMap(x => x.split("\\W+"))
      .map(x => x.toLowerCase())

    // Count up the occurrences of each word
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    val wordCountsSorted = wordCounts.sortBy(x => x._2)
//    val wordCountsSorted = wordCounts.map(x => (x._2, x._1)).sortByKey()

    // Print the results, flipping the (count, word) results to word: count as we go.
    for (result <- wordCountsSorted.collect()) {
//      val count = result._1
//      val word = result._2
      val count = result._2
      val word = result._1
      println(s"$word: $count")
    }
  }

}