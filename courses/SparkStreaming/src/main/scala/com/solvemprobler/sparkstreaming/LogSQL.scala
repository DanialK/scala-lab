package com.solvemprobler.sparkstreaming

import java.util.regex.{Matcher, Pattern}

import org.apache.spark.storage.StorageLevel

import scala.util.Try

object LogSQL extends SparkSessionWrapper {

  case class Record(url: String, status: Int, agent: String)

  val pattern: Pattern = Utilities.apacheLogPattern()
  val datePattern: Pattern = Pattern.compile("\\[(.*?) .+]")


  def main(args: Array[String]): Unit = {

    val lines = ssc.socketTextStream("127.0.0.1", 9999, StorageLevel.MEMORY_ONLY_SER)

    val requests = lines.map(x => {
      val matcher:Matcher = pattern.matcher(x)
      if (matcher.matches()) {
        val request = matcher.group(5)
        val requestFields = request.toString.split(" ")
        val url = Try(requestFields(0)) getOrElse "[error]"
        (url, matcher.group(6).toInt, matcher.group(9))
      } else {
        ("error", 0, "error")
      }
    })

    requests.foreachRDD(rdd => {
      val sqlContext = spark.sqlContext
      import sqlContext.implicits._

      val requestsDF = rdd.map(w => Record(w._1, w._2, w._3)).toDF()
      requestsDF.createOrReplaceTempView("requests")

      val wordCountsDF = sqlContext.sql(
        """
          | SELECT agent, COUNT(*) AS total FROM requests GROUP BY agent
        """.stripMargin)

      wordCountsDF.show()
    })

    // Kick it all off
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
