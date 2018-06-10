import reactive.BankAccount

object bankTest {
  val account = new BankAccount
  account.deposit(50)
  account.withdraw(20)
  account.withdraw(50)
}