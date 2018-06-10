package com.solvemprobler.spark

import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.util.LongAccumulator
import org.apache.log4j._
import scala.collection.mutable.ArrayBuffer

object DegreesOfSeparation {
  val startCharacterId = 5306 //SpiderMan
  val targetCharacterId = 14 //ADAM 3,031 (who?)

  var hitCounter:Option[LongAccumulator] = None

  type BFSData = (Array[Int], Int, String)
  type BFSNode = (Int, BFSData)

  def convertToBFS(line: String): BFSNode = {
    val values = line.split("\\s+")

    val heroId = values(0).toInt

    // Extract subsequent hero ID's into the connections array
    var connections: ArrayBuffer[Int] = ArrayBuffer()

    for (connection <- 1 until values.length) {
      connections += values(connection).toInt
    }

    // Default distance and color is 9999 and white
    var color:String = "WHITE"
    var distance:Int = 9999

    // Unless this is the character we're starting from
    if (heroId == startCharacterId) {
      color = "GRAY"
      distance = 0
    }

    val data: BFSData = (connections.toArray, distance, color)

    (heroId, data)
  }

  def createStartingRdd(sc:SparkContext): RDD[BFSNode] = {
    val inputFile = sc.textFile("../marvel-graph.txt")
    return inputFile.map(convertToBFS)
  }

  def bfsMap(node:BFSNode): Array[BFSNode] = {
    val characterId:Int = node._1
    val data:BFSData = node._2

    val connections:Array[Int] = data._1
    val distance:Int = data._2
    var color:String = data._3

    // This is called from flatMap, so we return an array
    // of potentially many BFSNodes to add to our new RDD
    var results:ArrayBuffer[BFSNode] = ArrayBuffer()

    if (color == "GRAY") {
      for (connection <- connections) {
        val newCharacterId = connection
        val newDistance = distance + 1
        val newColor = "GRAY"

        if (newCharacterId == targetCharacterId) {
          if (hitCounter.isDefined) {
            hitCounter.get.add(1)
          }
        }

        val newEntry:BFSNode = (newCharacterId, (Array(), newDistance, newColor))
        results += newEntry
      }

      color = "black"
    }

    // Add the original node back in, so its connections can get merged with
    // the gray nodes in the reducer.
    val thisEntry:BFSNode = (characterId, (connections, distance, color))
    results += thisEntry
    return results.toArray
  }

  /** Combine nodes for the same heroID, preserving the shortest length and darkest color. */
  def bfsReduce(data1:BFSData, data2:BFSData): BFSData = {
    // Extract data that we are combining
    val edges1:Array[Int] = data1._1
    val edges2:Array[Int] = data2._1
    val distance1:Int = data1._2
    val distance2:Int = data2._2
    val color1:String = data1._3
    val color2:String = data2._3

    // Default node values
    var distance:Int = 9999
    var color:String = "WHITE"
    var edges:ArrayBuffer[Int] = ArrayBuffer()

    // See if one is the original node with its connections.
    // If so preserve them.
    if (edges1.length > 0) {
      edges ++= edges1
    }
    if (edges2.length > 0) {
      edges ++= edges2
    }

    // Preserve minimum distance
    if (distance1 < distance) {
      distance = distance1
    }
    if (distance2 < distance) {
      distance = distance2
    }

    // Preserve darkest color
    if (color1 == "WHITE" && (color2 == "GRAY" || color2 == "BLACK")) {
      color = color2
    }
    if (color1 == "GRAY" && color2 == "BLACK") {
      color = color2
    }
    if (color2 == "WHITE" && (color1 == "GRAY" || color1 == "BLACK")) {
      color = color1
    }
    if (color2 == "GRAY" && color1 == "BLACK") {
      color = color1
    }

    return (edges.toArray, distance, color)
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val sc = new SparkContext("local[*]", "DegreesOfSeparation")

    hitCounter = Some(sc.longAccumulator("Hit Counter"))
    var iterationRdd = createStartingRdd(sc)

    for (iteration <- 1 to 10) {
      println("Running BFS Iteration# " + iteration)
      val mapped = iterationRdd.flatMap(bfsMap)
      println("Processing " + mapped.count() + " values.")

      if (hitCounter.isDefined) {
        val hitCount = hitCounter.get.value
        if (hitCount > 0) {
          println("Hit the target character! From " + hitCount +
            " different direction(s).")
          return
        }
      }

      // Reducer combines data for each character Id, preserving the darkest
      // color and shortest path.
      iterationRdd = mapped.reduceByKey(bfsReduce)
    }
  }
}
