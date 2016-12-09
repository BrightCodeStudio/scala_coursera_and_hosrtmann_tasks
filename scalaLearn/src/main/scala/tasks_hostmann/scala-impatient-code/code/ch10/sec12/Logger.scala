package tasks_hostmann.scala-impatient-code.code.ch10.sec12

trait Logged {
  def log(msg: String) { }
}

trait ConsoleLogger extends Logged {
  override def log(msg: String) { println(msg) }
}

trait LoggedException extends Exception with Logged {
  def log() { log(getMessage()) }
}

class UnhappyException extends IllegalStateException 
  with LoggedException { // This class extends a trait
  override def getMessage() = "arggh!"
}

class Account {
  protected var balance = 0.0
}

class SavingsAccount extends Account {
  def withdraw(amount: Double) {
    if (amount > balance) throw new UnhappyException with ConsoleLogger
    else balance -= amount
  }

  // More methods ...
}

object Main extends App {
  try {
    val acct = new SavingsAccount
    acct.withdraw(100)
  } catch {
    case e: LoggedException => e.log()
  }
}
