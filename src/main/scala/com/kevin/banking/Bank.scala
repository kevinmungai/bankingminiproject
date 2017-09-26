package com.kevin.banking

import akka.actor._

object Bank {

  def props: Props = Props[Bank]

  case object CheckBalance
  case class Balance(amount: Int)

  case class Deposit(amount: Int)

  case class Withdraw(amount: Int)

}

class Bank extends Actor with ActorLogging {

  import Bank._

  var balance = 0

  override def receive: Receive = {
    case CheckBalance =>
      log.info("Received balance Request!")
      sender() ! Balance(balance)
  }
}
