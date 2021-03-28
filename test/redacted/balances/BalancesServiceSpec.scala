package redacted.balances

import redacted.TestData
import redacted.operations.DepositOperation
import redacted.transactions.{Transaction, TransactionLog}

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

class BalancesServiceSpec extends FlatSpec with Matchers {
  behavior of s"${classOf[BalancesService]}"

  private object Balances extends BalancesService with TestData
  private object Accounts extends TestData

  it should "correctly find the balance for an account." in {
    val account = Accounts.account3Regular

    Balances.getBalanceForAccount(account.id) match {
      case Some(balance) =>
        balance shouldBe Balance(currentAmount = 90)
      case None =>
        fail(s"Was expecting a ${classOf[Balance]}.")
    }
  }

  it should "correctly find the balance details for an account." in {
    val account = Accounts.account1SavesALot

    Balances.getBalanceDetails(account.id) match {
      case Some(balanceDetails) =>
        val now: DateTime = DateTime.parse("2019-02-06T08:20:00Z")

        val log: TransactionLog = {
          val transactions = Seq(
            Transaction(DepositOperation(amount=BigDecimal(50.00), account, "Some savings."), commitDate = now),
            Transaction(DepositOperation(amount=BigDecimal(100.00), account, "Again, some savings."), commitDate = now.minusDays(2)),
            Transaction(DepositOperation(amount=BigDecimal(50.00), account, "More savings."), commitDate = now.minusDays(3))
          )

          TransactionLog(transactions)
        }

        balanceDetails shouldBe BalanceDetails(200.00, log)
      case None =>
        fail(s"Was expecting a ${classOf[BalanceDetails]}.")
    }
  }
}
