package com.solvemprobler.spark

import org.apache.log4j._
import org.apache.spark._

import scala.math.max

/** Find the minimum temperature by weather station */
object MaxTemperatures {

  def parseLine(line:String)= {
    val fields = line.split(",")
    val stationID = fields(0)
    val entryType = fields(2)
    val temperature = fields(3).toFloat * 0.1f * (9.0f / 5.0f) + 32.0f
    (stationID, entryType, temperature)
  }
  /** Our main function where the action happens */
  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "MaxTemperatures")

    // Read each line of input data
    val lines = sc.textFile("../1800.csv")

    // Convert to (stationID, entryType, temperature) tuples
    val parsedLines = lines.map(parseLine)

    // Filter out all but TMIN entries
    val minTemps = parsedLines.filter(x => x._2 == "TMAX")

    // Convert to (stationID, temperature)
    val stationTemps = minTemps.map(x => (x._1, x._3))

    // Reduce by stationID retaining the minimum temperature found
    val minTempsByStation = stationTemps.reduceByKey((x,y) => max(x, y))

    // Collect, format, and print the results
    val results = minTempsByStation.collect()

    for {
      result <- results.sorted
      station = result._1
      temp = result._2
      formattedTemp = f"$temp%.2f F"
    } yield println(s"$station minimum temperature: $formattedTemp")
  }
}