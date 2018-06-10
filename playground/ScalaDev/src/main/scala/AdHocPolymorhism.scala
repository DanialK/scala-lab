/*
* Ad-Hoc Polymorphism with implicit conversions
* */
object AdHocPolymorhism extends App {
  trait Appendable[A] {
    def append(a: A): A
  }

  class AppendableInt(i: Int) extends Appendable[Int] {
    override def append(a: Int) = i + a
  }

  class AppendableString(s: String) extends Appendable[String] {
    override def append(a: String) = s.concat(a)
  }

//  def appendItems[A](a: A, b: A)(implicit ev: A => Appendable[A]) =
//    a append b

  /*
  * Re-writing this using "view bounds"
  * [A <% Appendable[A]] is the view bound
  * Read: method appendItems() is parameterized with A and it requires that there
  *       must be an implicit conversion from A to Appendable[A] available in scope
  * */
  def appendItems[A <% Appendable[A]](a: A, b: A) = a append b

  implicit def toAppendable(i: Int) = new AppendableInt(i)
  implicit def toAppendable(s: String) = new AppendableString(s)
}