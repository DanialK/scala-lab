package com.solvemprobler.sparkstreaming

import java.util.regex.Matcher
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds
import scala.util.Try

object LogAlarmer extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {

    val pattern = Utilities.apacheLogPattern()

    val lines = ssc.socketTextStream("127.0.0.1", 9999, StorageLevel.MEMORY_ONLY_SER)

    val statuses = lines.map(x => {
      val matcher:Matcher = pattern.matcher(x)
      if (matcher.matches()) {
        matcher.group(6) // Status code field
      } else {
        "[error]"
      }
    })

    val successFailure = statuses.map(x => {
      val statusCode = Try(x.toInt) getOrElse 0
      if (statusCode >= 200 && statusCode < 300) {
        "Success"
      } else if(statusCode >= 500 && statusCode < 600) {
        "Failure"
      } else {
        "Other"
      }
    })

    val statusCounts = successFailure
        .countByValueAndWindow(Seconds(300), Seconds(1))

    statusCounts.foreachRDD((rdd, time) => {
      var totalSuccess: Long = 0
      var totalFailure: Long = 0

      if (rdd.count() > 0) {
        rdd.collect().foreach { case (status, count) =>
          status match {
            case "Success" => totalSuccess += count
            case "Failure" => totalFailure += count
            case _ => Unit
          }
        }
      }

      println(s"Total Success: $totalSuccess , Total Failure: $totalFailure")

      if (totalSuccess + totalFailure > 100) {
        val ratio = Try(totalFailure.toDouble / totalSuccess.toDouble) getOrElse 1.0

        if (ratio > 0.5) {
          println("WAKE SOMEBODY UP!")
        } else {
          println("All g in the hood")
        }
      }

    })

    // Kick it all off
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
