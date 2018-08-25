import org.apache.spark._
import org.apache.spark.h2o._
import java.io.File

import hex.tree.gbm.GBM
import hex.tree.gbm.GBMModel.GBMParameters
import org.apache.spark.sql.SparkSession
import water.fvec.H2OFrame


object TestApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
        .builder
        .appName("H2ODev")
        .master("local[*]")
        .getOrCreate()
    val conf = new H2OConf(spark)
    val h2oContext = H2OContext.getOrCreate(spark, conf)
    import h2oContext.implicits._

    // Register file to be available on all nodes
    spark.sparkContext.addFile(new File("data/iris.csv").getAbsolutePath)

    // Load data and parse it via h2o parser
    val irisTable = new H2OFrame(new File(SparkFiles.get("iris.csv")))

    // Build GBM model
    val gbmParams = new GBMParameters()
    gbmParams._train = irisTable
    gbmParams._response_column = "class"
    gbmParams._ntrees = 5

    val gbm = new GBM(gbmParams)
    val gbmModel = gbm.trainModel.get


    val predict = gbmModel.score(irisTable).subframe(Array("predict"))

    // Compute number of mis-predictions with help of Spark API
    val trainRDD = h2oContext.asRDD[StringHolder](irisTable.subframe(Array("class")))
    val predictRDD = h2oContext.asRDD[StringHolder](predict)
    assert(trainRDD.count() == predictRDD.count)
    val numMispredictions = trainRDD.zip(predictRDD).filter( i => {
      val act = i._1
      val pred = i._2
      act.result != pred.result
    }).collect()

    println(
      s"""
         |Number of mispredictions: ${numMispredictions.length}
         |
         |Mispredictions:
         |
         |actual X predicted
         |------------------
         |${numMispredictions.map(i => i._1.result.get + " X " + i._2.result.get).mkString("\n")}
       """.stripMargin)

    // Shutdown application
    spark.stop()
  }
}
