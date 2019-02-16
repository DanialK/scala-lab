package com.solvemprobler.sparkstreaming

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._

object StructuredStreaming extends SparkSessionWrapper {

  case class LogEntry(ip: String, client: String, user: String, dateTime: String, request: String, status: String, bytes: String, referer: String, agent: String)

  val pattern: Pattern = Utilities.apacheLogPattern()
  val datePattern: Pattern = Pattern.compile("\\[(.*?) .+]")

  def parseDateField(field: String): scala.Option[String] = {
    val dateMatcher = datePattern.matcher(field)
    if (dateMatcher.find()) {
      val dateString = dateMatcher.group(1)
      val dateFormat = new SimpleDateFormat("dd/MMM/yyy:HH:mm:ss", Locale.ENGLISH)
      val date = dateFormat.parse(dateString)
      val timestamp = new Timestamp(date.getTime)
      Option(timestamp.toString)
    } else
      None
  }

  def parseLog(x: Row): scala.Option[LogEntry] = {
    val matcher = pattern.matcher(x.getString(0))
    if (matcher.matches()) {
      Some(LogEntry(
        matcher.group(1),
        matcher.group(2),
        matcher.group(3),
        parseDateField(matcher.group(4)).getOrElse(""),
        matcher.group(5),
        matcher.group(6),
        matcher.group(7),
        matcher.group(8),
        matcher.group(9)
      ))
    } else
      None
  }

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val rawData = spark.readStream.text("logs")

    val structuredData = rawData
      .flatMap(parseLog)
      .select("status", "dateTime")

    val windowed = structuredData
      .groupBy($"status", window($"dateTime", "1 hour"))
      .count()
      .orderBy("window")

    val query = windowed
      .writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()

    spark.stop()
  }
}
