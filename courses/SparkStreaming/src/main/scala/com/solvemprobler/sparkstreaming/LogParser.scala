package com.solvemprobler.sparkstreaming

import java.util.regex.{Matcher, Pattern}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Seconds

object LogParser extends SparkSessionWrapper {
  def main(args: Array[String]): Unit = {

    val pattern = apacheLogPattern()

    val lines = ssc.socketTextStream("127.0.0.1", 9999, StorageLevel.MEMORY_ONLY_SER)

    val requests = lines.map(x => {
      val matcher:Matcher = pattern.matcher(x)
      if (matcher.matches()) {
        matcher.group(5) // request field
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
        .reduceByKeyAndWindow(_+_, _-_, Seconds(300), Seconds(1))

    val sortedResults = urlsCounts.transform(rdd => rdd.sortBy(x => x._2, ascending = false))

    sortedResults.print()

    // Kick it all off
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }


  def apacheLogPattern(): Pattern = {
    val ddd = "\\d{1,3}"
    val ip = s"($ddd\\.$ddd\\.$ddd\\.$ddd)?"
    val client = "(\\S+)"
    val user = "(\\S+)"
    val dateTime = "(\\[.+?\\])"
    val request = "\"(.*?)\""
    val status = "(\\d{3})"
    val bytes = "(\\S+)"
    val referer = "\"(.*?)\""
    val agent = "\"(.*?)\""
    val regex = s"$ip $client $user $dateTime $request $status $bytes $referer $agent"
    Pattern.compile(regex)
  }
}
