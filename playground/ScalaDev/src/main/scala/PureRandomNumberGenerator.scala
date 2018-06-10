object PureRandomNumberGenerator extends App {
  trait RNG {
    def nextInt: (Int, RNG)
  }

  case class SimpleRNG(seed: Long) extends RNG {
    override def nextInt: (Int, RNG) = {
      val newSeed = (seed * 0x5DEECE6DL + 0xBL) & 0xFFFFFFFFFFFL
      val nextRNG = SimpleRNG(newSeed)
      val n = (newSeed >>> 16).toInt
      (n, nextRNG)
    }
  }

  type State[S, +A] = S => (A, S)

  type Rand[+A] = State[RNG, A]

  def unit[S, A](a: A): State[S, A] =
    rng => (a, rng)

//  def map[S, A, B](s: State[S, A])(f: A => B): State[S, B] =
//    rng => {
//      val (a, rng2) = s(rng)
//      (f(a), rng2)
//    }
  def map[S, A, B](s: State[S, A])(f: A => B): State[S, B] =
    flatMap(s)(a => unit(f(a)))

//  def map2[S, A, B, C](ra: State[S, A], rb: State[S, B])(f: (A, B) => C): State[S, C] =
//    rng => {
//      val (a, rng1) = ra(rng)
//      val (b, rng2) = rb(rng1)
//      (f(a, b), rng2)
//    }
  def map2[S, A, B, C](ra: State[S, A], rb: State[S, B])(f: (A, B) => C): State[S, C] =
    flatMap(ra)(a => map(rb)(b => f(a, b)))

  def flatMap[S, A, B](s: State[S, A])(f: A => State[S, B]): State[S, B] =
    rng => {
      val (a, rng2) = s(rng)
      f(a)(rng2)
    }

  def sequence[S, A](fs: List[State[S, A]]): Rand[State[S, A]] = ???


}
