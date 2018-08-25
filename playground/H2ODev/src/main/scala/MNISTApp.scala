import java.io.File

import hex.deeplearning.DeepLearning
import hex.deeplearning.DeepLearningModel.DeepLearningParameters
import org.apache.spark.h2o._
import org.apache.spark.sql._
import org.apache.spark.sql.types._


object MNISTApp {
  def getSchema(): String = {

    var schema = "Label "
    val limit = 28*28

    for (i <- 1 to limit){
      schema += "P" + i.toString + " "
    }

    schema
  }

  val schemaString = getSchema()
  val schema = StructType(schemaString.split(" ")
    .map(fieldName => StructField(fieldName, IntegerType, false)))

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
        .builder
        .appName("H2ODev")
        .master("local[*]")
        .getOrCreate()
    val conf = new H2OConf(spark)
    val h2oContext = H2OContext.getOrCreate(spark, conf)
    import h2oContext.implicits._

    h2oContext.openFlow

    val rawTrainData = spark.sparkContext.textFile(new File("data/mnist_train.csv").getAbsolutePath)
    val rawTestData = spark.sparkContext.textFile(new File("data/mnist_test.csv").getAbsolutePath)

    val trainRDD  = rawTrainData.map(rawRow => Row(rawRow.split(",").map(_.toInt): _* ))
    val testRDD  = rawTestData.map(rawRow => Row.fromSeq(rawRow.split(",").map(_.toInt)))

    val trainDf = spark.sqlContext.createDataFrame(trainRDD, schema)
    val testDf = spark.sqlContext.createDataFrame(testRDD, schema)

    val trainFrame = h2oContext.asH2OFrame(trainDf)
    trainFrame.replace(trainFrame.find("Label"), trainFrame.vec("Label").toCategoricalVec)
    trainFrame.update()

    val testFrame = h2oContext.asH2OFrame(testDf)
    testFrame.replace(testFrame.find("Label"), testFrame.vec("Label").toCategoricalVec)
    testFrame.update()

    val dlParams = new DeepLearningParameters()
    dlParams._epochs               = 10
    dlParams._train                = trainFrame
    dlParams._valid                = testFrame
    dlParams._response_column      = "Label"
    dlParams._variable_importances = true
    val dl = new DeepLearning(dlParams)
    val dlModel = dl.trainModel.get

    val testResArray = testDf.collect()
    val sizeResults  = testResArray.length
    val resArray = new Array[Double](sizeResults)

    for ( i <- resArray.indices) {
      resArray(i) = testFrame.vec("Label").at(i)
    }


    val testH2oPredict = dlModel.score(testFrame)('predict)
    val testPredictions = h2oContext.asRDD[DoubleHolder](testH2oPredict)
      .collect.map(_.result.getOrElse(Double.NaN))

    var resAccuracy = 0
    for (i <- resArray.indices) {
      if (resArray(i) == testPredictions(i))
        resAccuracy = resAccuracy + 1
    }

    println()
    println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" )
    println( ">>>>>> Model Test Accuracy = "
      + 100 * resAccuracy / resArray.length  + " % " )
    println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" )
    println()

    // Register file to be available on all nodes
    // Shutdown application
    h2oContext.stop()
    spark.stop()
  }
}
