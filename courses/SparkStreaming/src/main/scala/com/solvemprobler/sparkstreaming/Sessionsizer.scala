package com.solvemprobler.sparkstreaming

import java.util.regex.Matcher

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._

import scala.util.Try

object Sessionsizer extends SparkSessionWrapper {

  case class SessionData(sessionLength: Long, clickStream: List[String])

  def trackStateFunc(batchTime: Time, ip: String, url: Option[String],
                     state: State[SessionData]): Option[(String, SessionData)] = {
    val previousState = state.getOption().getOrElse(SessionData(0, List()))

    val newState = SessionData(
      previousState.sessionLength + 1L,
      (previousState.clickStream :+ url.getOrElse("empty")).take(10)
    )

    state.update(newState)

    Some((ip, newState))
  }

  def main(args: Array[String]): Unit = {

    val pattern = Utilities.apacheLogPattern()

    val stateSpec = StateSpec.function(trackStateFunc _).timeout(Minutes(30))

    val lines = ssc.socketTextStream("127.0.0.1", 9999, StorageLevel.MEMORY_ONLY_SER)

    val requests = lines.map(x => {
      val matcher:Matcher = pattern.matcher(x)
      if (matcher.matches()) {
        val ip = matcher.group(1)
        val request = matcher.group(5)
        val requestFields = request.toString.split(" ")
        val url = Try(requestFields(1)) getOrElse "[error]"
        (ip, url)
      } else {
        ("error", "error")
      }
    })

    val requestsWithState = requests.mapWithState(stateSpec)
    val stateSnapshotStream = requestsWithState.stateSnapshots()

    stateSnapshotStream.foreachRDD((rdd, time) => {
      val sqlContext = spark.sqlContext
      import sqlContext.implicits._

      val requestDF = rdd
        .map(x => (x._1, x._2.sessionLength, x._2.clickStream))
        .toDF("ip", "sessionLength", "clickStream")

      requestDF.createOrReplaceTempView("sessionData")

      val sessionsDF = sqlContext.sql("select * from sessionData")

      sessionsDF.show()
    })

    // Kick it all off
    ssc.checkpoint("checkpoint")
    ssc.start()
    ssc.awaitTermination()
  }

}
