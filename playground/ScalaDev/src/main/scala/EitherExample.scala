object EitherExample extends App {
  def validate(tag: String, value: String) =
    if (value.nonEmpty) Right(value) else Left(s"$tag is empty")

  def validateName(first: String, last: String) =
    for {
      first <- validate("First name", first)
      last <- validate("Last name", last)
    } yield s"$first $last"

  println(validateName("Danial", ""))
  println(validateName("Danial", "K"))

  List(validateName("Danial", "K"), validateName("Danial", "")) foreach {
    case x => x match {
      case Left(_) => println("NOPE")
      case Right(value) => println(s"Welcome $value")
    }
  }
}
