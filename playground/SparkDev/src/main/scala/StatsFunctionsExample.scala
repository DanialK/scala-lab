import org.apache.spark.sql.functions._

object StatsFunctionsExample extends SparkSessionWrapper {

  def main(args: Array[String]): Unit = {
    import spark.implicits._


    /*
    * TODO:
    * - describe
    * - cross_tab
    * */

    spark.stop()
  }
}
