import math.Ordering
import scala.util.Sorting

object OrderingTypeClass extends App {
  val myNumericList = List("2", "11", "4", "22")
  val myAlphabeticalList = List("2", "11", "4", "22")


//  implicit val numericOrdering = new Ordering[String] {
//    def compare(a: String, b: String) = a.toInt.compare(b.toInt)
//  }
//  println(myNumericList.sorted)


  implicit val alphabeticalOrdering = new Ordering[String] {
    def compare(a: String, b: String) = a.compare(b)
  }
  println(myNumericList.sorted)
}
