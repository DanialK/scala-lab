package fpinscala.datastructures
/*
* Chapter 3 of Functional Programming in Scala
* */

sealed trait MyList[+A]

case object Nil extends MyList[Nothing]
case class Cons[+A](head: A, tail: MyList[A]) extends MyList[A]

object MyList {
  def sum(ints: MyList[Int]): Int = ints match {
    case Nil => 0
    case Cons(x, xs) => x + sum(xs)
  }

  def product(ints: MyList[Int]): Int = ints match {
    case Nil => 1
    case Cons(0.0, _) => 0
    case Cons(x, xs) => x * product(xs)
  }

  def head[A](l: MyList[A]): A = l match {
    case Nil => sys.error("empty list")
    case Cons(x, _) => x
  }

  def tail[A](l: MyList[A]): MyList[A] = l match {
    case Nil => sys.error("tail of empty list")
    case Cons(_, xs) => xs
  }

  def drop[A](l: MyList[A], n: Int): MyList[A] = n match {
    case 0 => l
    case _ => l match {
      case Nil => Nil
      case Cons(_, xs) => drop(xs, n - 1)
    }
  }

  def dropWhile[A](l: MyList[A], f: A => Boolean): MyList[A] = l match {
    case Cons(h,t) if f(h) => dropWhile(t, f)
    case _ => l
  }
  // Can't infer the type of the predicate paramter
  // e.g dropWhile(list, (x: Int) => x < 4)

//  def dropWhile[A](l: MyList[A])(f: A => Boolean): List[A] = l match {
//    case Cons(h,t) if f(h) => dropWhile(t)(f)
//    case _ => l
//  }
  // Can infer the type of the predicate parameter
  // e.g dropWhile(list)(x => x < 4)

  def append[A](l1: MyList[A], l2: MyList[A]): MyList[A] = l1 match {
    case Nil => l2
    case Cons(x, xs) => Cons(x, append(xs, l2))
  }

  def init[A](l: MyList[A]): MyList[A] = l match {
    case Nil => sys.error("init of empty list")
    case Cons(_, Nil) => Nil
    case Cons(x, xs) => Cons(x, init(xs))
  }

  def foldRight[A, B](as: MyList[A], z: B)(f: (A, B) => B): B = as match {
    case Nil => z
    case Cons(x, xs) => f(x, foldRight(xs, z)(f))
  }

  def sum2(ns: MyList[Int]): Int = foldRight(ns, 0)(_+_)

  def product2(ns: MyList[Double]): Double = foldRight(ns, 1.0)(_*_)


  def apply[A](as: A*): MyList[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))

  implicit class MyListSyntax(list: MyList[Int]) {
    def sum(): Int = MyList.sum(list)
    def product(): Int = MyList.product(list)
    def head() = MyList.head(list)
    def tail(): MyList[Int] = MyList.tail(list)
    def drop(n: Int) = MyList.drop(list, n)
    def init() = MyList.init(list)
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val a = MyList(1, 2, 3, 4)
    println(a.sum())
    println(a.product())
    println(a.head)
    println(a.tail)
    println(a.drop(2))
    println(a.init)

    /*
    * TODO:
    * Read and do all the excises here
    * https://github.com/fpinscala/fpinscala/blob/master/answers/src/main/scala/fpinscala/datastructures/List.scala
    * */
  }
}