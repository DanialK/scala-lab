package com.solvemprobler.spark

import org.apache.log4j._
import org.apache.spark.ml.feature.LabeledPoint
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.sql.SparkSession

object LinearRegressionDataFrames {
  def parse(str: String): LabeledPoint = {
    val fields = str.split(",")
    LabeledPoint(fields(0).toDouble, Vectors.dense(fields(1).toDouble))
  }

  /** Our main function where the action happens */
  def main(args: Array[String]) {
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Create a SparkContext using every core of the local machine
    val spark = SparkSession
      .builder()
      .appName("LinearRegressionDataFrames")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    // Convert input data to LabeledPoints for MLLib
    val data = spark.sparkContext
        .textFile("../regression.txt")
        .map(parse)
        .toDF()

    val trainTestSplit = data.randomSplit(Array(0.5, 0.5))
    val trainingData = trainTestSplit(0)
    val testData = trainTestSplit(1)

    // Now we will create our linear regression model
    val lr = new LinearRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)

    val model = lr.fit(trainingData)

    // Predict values for our test feature data using our linear regression model
    val predictions = model.transform(testData).cache()

    predictions.select("prediction", "label").show()

    spark.stop()
  }
}