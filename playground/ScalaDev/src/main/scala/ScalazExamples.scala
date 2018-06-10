import scalaz._
import std.option._, std.list._

object ScalazExamples extends App {
  Apply[Option].apply2(some(1), some(2))((a, b) => a + b)
}
