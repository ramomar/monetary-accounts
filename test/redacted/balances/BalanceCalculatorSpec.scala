package redacted.balances

import java.util.UUID

import redacted.accounts.{Account, AccountType, StandardAccountType}
import redacted.operations.{DepositOperation, WithdrawalOperation}
import redacted.transactions.{Transaction, TransactionLog}

import org.joda.time.DateTime
import org.scalatest._


class BalanceCalculatorSpec extends FlatSpec with Matchers {
  behavior of s"${BalanceCalculator.getClass}"

  it should s"be able to compute a ${classOf[Balance]} out of a redacted.transactions log." in {
    val account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }

    val transactionsLog: TransactionLog = TransactionLog(Seq(
      Transaction(WithdrawalOperation(BigDecimal(50.00), account, "Dinner with friends"), DateTime.now),
      Transaction(DepositOperation(BigDecimal(200.00), account, "Some money a friend owed me"), DateTime.now.minusDays(1)),
      Transaction(WithdrawalOperation(BigDecimal(100.00), account, "Rent"), DateTime.now.minusDays(2)),
      Transaction(DepositOperation(BigDecimal(150.00), account, "The money I borrowed from my brother"), DateTime.now.minusDays(3)),
      Transaction(DepositOperation(BigDecimal(50.00), account, "Some savings"), DateTime.now.minusDays(4))
    ))

    BalanceCalculator.computeBalance(transactionsLog) shouldBe 250.00
  }
}
