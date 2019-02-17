import akka.actor.{Actor, ActorSystem, Props}


object MusicController {
  sealed trait ControllerMsg
  case object Play extends ControllerMsg
  case object Stop extends ControllerMsg

  def props: Props = Props[MusicController]
}

class MusicController extends Actor {
  import MusicController._
  def receive: PartialFunction[Any, Unit] = {
    case Play => println("Music started")
    case Stop => println("Music stopped")
    case _ => println("Nothing")
  }
}

object MusicPlayer {
  sealed trait PlayMsg
  case object StartMusic extends PlayMsg
  case object StopMusic extends PlayMsg
}

class MusicPlayer extends Actor {
  import MusicPlayer._
  def receive: PartialFunction[Any, Unit] = {
    case StartMusic =>
      val controller = context.actorOf(MusicController.props, "controller")
      controller ! MusicController.Play
    case StopMusic => println("I don't want to stop the music")
  }
}

object Creation extends App {
  val system = ActorSystem("creation")
  val player = system.actorOf(Props[MusicPlayer], "player")
  player ! MusicPlayer.StartMusic

  sys.addShutdownHook(new Thread() {
    override def run(): Unit  = {
      player ! MusicPlayer.StopMusic
    }
  }.run())
}