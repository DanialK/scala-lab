package recfun

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
    def pascal(c: Int, r: Int): Int = {
      if (r == 0 & c == 0) 1
      else if (r == 0) 0
      else pascal(c - 1, r-1) + pascal(c, r-1)
    }
  
  /**
   * Exercise 2
   */
    def balance(chars: List[Char]): Boolean = {
      def check(chars: List[Char], openCount: Int): Boolean = {
        if (chars.isEmpty) {
          return openCount == 0
        }
        val first = chars.head
        val n =
          if (first == '(') openCount + 1
          else if (first == ')') openCount - 1
          else openCount

        if (n >= 0) check(chars.tail, n)
        else false
      }

      check(chars, 0)
    }
  
  /**
   * Exercise 3
   */
    def countChange(money: Int, coins: List[Int]): Int = {
      if (coins.isEmpty || money < 0) {
        return 0
      }
      if (money == 0) {
        return 1
      }
      return countChange(money - coins.head, coins) + countChange(money, coins.tail)
    }
  }
