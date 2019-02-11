package com.solvemprobler.twitterstreaming

import java.util.Date

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.internal.Logging
import org.apache.log4j._
import org.apache.spark.streaming.twitter.TwitterUtils

object StreamingApp extends App {
  override def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.ERROR)
    val logger = Logger.getLogger(StreamingApp.getClass.getName)

    val config = new SparkConf()
      .setMaster("local[*]")
      .setAppName("twitter-stream-sentiment")
    val spark = SparkSession.builder().config(config).getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    val ssc = new StreamingContext(spark.sparkContext, Seconds(5))

    System.setProperty("twitter4j.oauth.consumerKey", "pIoudfYJj7dUnOpEFldOOJVJr")
    System.setProperty("twitter4j.oauth.consumerSecret", "EfQCd26CJTp1qslMXrK8j2ONcpC5RdbJoxZKnzEf2QY5AA7Wso")
    System.setProperty("twitter4j.oauth.accessToken", "41543977-pEM6ACjWgjvpDveNGiAdptCT5O1WOzmqCOJXbxOdS")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "ZFed01WcT4o7vkncOnP7XahPC8sV75yVuQDS2XBuAPYM9")


    val stream = TwitterUtils.createStream(ssc, None)


    val tags = stream.flatMap { status =>
      status.getHashtagEntities.map(_.getText)
    }

    tags
      .countByValue()
      .foreachRDD { rdd =>

        val now = new Date().getTime
        rdd
          .sortBy(_._2)
          .map(x => (x, now))
          .saveAsTextFile(s"twitter/$now")
      }
  }


}
