package com.solvemprobler.sparkstreaming

import java.util.Date
import org.apache.spark.streaming.twitter.TwitterUtils

object StreamingApp extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {

    Utilities.setupTwitter()

    val stream = TwitterUtils.createStream(ssc, None)

    val statuses = stream.map(status => status.getText)

    statuses
      .countByValue()
      .foreachRDD { rdd =>
        if (rdd.count() > 0) {
          val now = new Date().getTime
          rdd
            .sortBy(_._2)
            .map(x => (x, now))
            .saveAsTextFile(s"twitter/$now")
        }
      }

    // Kick it all off
    ssc.start()
    ssc.awaitTermination()
  }
}
