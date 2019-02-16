package com.solvemprobler.sparkstreaming

import java.util.regex.{Matcher, Pattern}

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.kafka._

object KafkaExample extends SparkSessionWrapper {
  val pattern: Pattern = Utilities.apacheLogPattern()
  def main(args: Array[String]): Unit = {
    val kafkaParams = Map("metadata.broker.list" -> "localhost:9092")

    val topics = List("testLogs").toSet

    val lines = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaParams,
      topics
    ).map(_._2)

    val requests = lines.map(x => {
      val matcher:Matcher = pattern.matcher(x)
      if (matcher.matches()) {
        matcher.group(5) // Status code field
      } else {
        "[error]"
      }
    })

    val urls = requests.map(x => {
      val arr = x.toString.split(" ")
      if (arr.size == 3)
        arr(1)
      else
        "[error]"
    })

    val urlsCounts = urls
      .map(x => (x, 1))
      .reduceByKeyAndWindow(_+_, _-_, Seconds(300), Seconds(3))

    val sortedResults = urlsCounts.transform(rdd => rdd.sortBy(x => x._2, ascending = false))

    sortedResults.print()

    // Kick it all off
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
