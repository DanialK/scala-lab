package com.solvemprobler.sparkstreaming

import java.util.concurrent.atomic._
import org.apache.spark.streaming.twitter.TwitterUtils

object AverageTweetLength extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {

    Utilities.setupTwitter()

    val stream = TwitterUtils.createStream(ssc, None)

    val statuses = stream.map(tweet => tweet.getText)
    val lengths = statuses.map(status => status.length)

    val totalTweets = new AtomicLong(0)
    val totalChars = new AtomicLong(0)

    lengths
      .foreachRDD { rdd =>
        val count = rdd.count()
        if (count > 0) {
          totalTweets.getAndAdd(count)
          totalChars.getAndAdd(rdd.reduce((x, y) => x + y))
          println("Total tweets: " + totalTweets.get() +
            " Total characters: " + totalChars.get() +
            " Average: " + totalChars.get() / totalTweets.get()
          )
        }
      }

    // Kick it all off
    ssc.start()
    ssc.awaitTermination()
  }
}
