import cats.data.Kleisli
import cats.effect.IO
import cats.implicits._
import scala.util.Try
import scala.util.Either
import scala.language.implicitConversions

case class User(id: Int, name: String, friends: List[User])
case class Article(authorId: Int, text: String)

object KleisliExample extends App {

  def readUsers(): IO[List[User]] = IO {
    val source = scala.io.Source.fromFile("data/user.txt")
    val lines = try source.mkString finally source.close()
    lines.split("\n").map(x => {
      val line = x.split(",")
      User(line(0).toInt, line(1),  List())
    }).toList
  }

  def readArticles(): IO[List[Article]] = IO {
    val source = scala.io.Source.fromFile("data/article.txt")
    val lines = try source.mkString finally source.close()
    lines.split("\n").map(x => {
      val line = x.split(",")
      Article(line(0).toInt, line(1))
    }).toList
  }

  override def main(args: Array[String]): Unit = {
    println("Danial")
    val users = readUsers()
    val articles = readArticles()
    users
    println(users.unsafeRunSync())
  }
}