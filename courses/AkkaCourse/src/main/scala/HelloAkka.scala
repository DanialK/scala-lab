
import akka.actor.{Actor, ActorSystem, Props}

case class WhoToGreet(who: String)

class Greeter extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case WhoToGreet(who) => println(s"Hello $who")
  }
}

object HelloAkka extends App {
  val system = ActorSystem("Hello-Akka")

  val greater = system.actorOf(Props[Greeter], "greater")

  greater ! WhoToGreet("Akka")
}