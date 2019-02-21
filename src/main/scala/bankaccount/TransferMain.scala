package bankaccount

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive

class TransferMain extends Actor {
  val accountA: ActorRef = context.actorOf(Props[BankAccount], "accountA")
  val accountB: ActorRef = context.actorOf(Props[BankAccount], "accountB")

  accountA ! BankAccount.Deposit(100)

  override def receive: Receive = LoggingReceive {
    case BankAccount.Done => transfer(160)
  }

  def transfer(amount: BigInt): Unit = {
    val transaction = context.actorOf(Props[WireTransfer], "transfer")
    transaction ! WireTransfer.Transfer(accountA, accountB, amount)
    context.become(LoggingReceive {
      case WireTransfer.Done =>
        println("Success!")
        context.stop(self)
      case WireTransfer.Failed =>
        println("Failed :(")
        context.stop(self
        )
    })
  }
}
