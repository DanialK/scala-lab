package com.solvemprobler.spark

import org.apache.log4j._
import org.apache.spark._

object TotalSpentByCustomer {
  def parseLine(line: String) = {
    val fields = line.split(",")
    val customerId = fields(0).toInt
    val spent = fields(2).toDouble
    (customerId, spent)
  }

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val sc = new SparkContext("local[*]", "TotalSpentByCustomer")

    val input = sc.textFile("../customer-orders.csv")

    val transactions = input.map(parseLine)

    val totalByCustomer = transactions.reduceByKey(_+_)

    val result = totalByCustomer.sortBy(x => x._2).collect()

    result.foreach(println)
  }
}
