package fpmax

// Fully imperative. Refactoring is necessary.

object App0 {

  def readLine(): String = scala.io.StdIn.readLine()

  def main(args: Array[String]): Unit = {
    println("What is your name?")

    val name = readLine()

    println("Hello, " + name + ", welcome to the game!")

    var exec = true

    while (exec) {
      val num = scala.util.Random.nextInt(5) + 1

      println("Dear " + name + ", please guess a number from 1 to 5:")

      val guess = readLine().toInt

      if (guess == num) println("You guessed right, " + name + "!")
      else println("You guessed wrong, " + name + "! The number was: " + num)

      println("Do you want to continue, " + name + "?")

      readLine() match {
        case "y" => exec = true
        case "n" => exec = false
      }
    }
  }
}
