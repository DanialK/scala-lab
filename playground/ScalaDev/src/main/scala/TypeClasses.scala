/*
* Ad-Hoc Polymorphism with TypeClasses
* Notes from: https://medium.com/@sinisalouc/ad-hoc-polymorphism-and-type-classes-442ae22e5342
* Type class is a concept of having
*   - type-dependent interface and implicit implementations of that interface
*   - with separate implementation for each supported type
* In Scala the best way to model this is to use a parameterized trait and to put implicit implementations
* of that trait for supported types in the trait’s companion object (instead of implicit values
* you can also use implicit objects (see AdHocPolymorphism.scala), but why pollute the
* namespace with extra types if you don’t have to)
*
* Type classes make it easy to add implementations for new types or to override
* implementations for existing types. Since these implementations are implicit,
* one must take into account precedence of implicits in Scala when reasoning about
* which one will be actually used
* */
object TypeClasses extends App {
  trait Appendable[A] {
    def append(a: A, b: A): A
  }

  object Appendable {
    implicit val appendableInt = new Appendable[Int] {
      override def append(a: Int, b: Int) = a + b
    }
    implicit val appendableString = new Appendable[String] {
      override def append(a: String, b: String) = a.concat(b)
    }
  }

//  def appendItems[A](a: A, b: A)(implicit ev: Appendable[A]) =
//    ev.append(a, b)

  /*
  * Re-writing this using "context bounds"
  * Read: method foo() is parameterized with A and it requires that there must be an
  *       implicit value Appendable[A] available in scope.
  * Syntax: implicitly it is used to reference an instance of Appendable[A]
  *         that has been provided (since we no longer have “ev”).
  * */
  def appendItems[A : Appendable](a: A, b: A) =
    implicitly[Appendable[A]].append(a, b)



  // Sometime later, create a new instance of the type class

  implicit val appendableDouble = new Appendable[Double] {
    override def append(a: Double, b: Double) = a + b
  }

  println(appendItems(1.5, 2.5))
}