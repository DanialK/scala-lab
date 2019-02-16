package com.solvemprobler.sparkstreaming

import java.util.concurrent.atomic._

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.twitter.TwitterUtils


object MostPopularTweet extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {

    Utilities.setupTwitter()

    val stream = TwitterUtils.createStream(ssc, None)

    val hashTags = stream.flatMap(status => status.getHashtagEntities.map(_.getText))
    val hashTagsKeyValues = hashTags.map(hashTag => (hashTag, 1))

    val hashTagCounts = hashTagsKeyValues
      .reduceByKeyAndWindow(
        reduceFunc = _ + _,
        invReduceFunc = _ - _,
        windowDuration = Seconds(10),
        slideDuration = Seconds(2)
      )
    val sortedResults = hashTagCounts.transform(rdd => rdd.sortBy(_._2, ascending = false))

    sortedResults.print(10)

    // Kick it all off
    ssc.checkpoint("./checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }
}
