package com.solvemprobler.sparkstreaming

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

trait SparkSessionWrapper {
  Logger.getLogger("org").setLevel(Level.ERROR)
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .appName("SparkDev")
      .master("local[*]")
      .config("spark.sql.warehouse.dir", "file:///C:/temp")
      .config("spark.sql.streaming.checkpointLocation", "file:///C:/checkpoint")
      .getOrCreate()
  }
  
  lazy val ssc = new StreamingContext(spark.sparkContext, Seconds(1))
}
