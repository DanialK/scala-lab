object Streams {
  def from (n: Int): Stream[Int] = n #:: from(n + 1)
  val nats = from(0)
  val m4s = nats map (_ * 4)

  nats.take(100).toList

  def srootStream(x: Double): Stream[Double] = {
    def improve(guess: Double) = (guess + x/guess) / 2
    lazy val guesses: Stream[Double] = 1 #:: (guesses map improve)
    return guesses
  }


  srootStream(4).take(100).toList
}