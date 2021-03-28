package redacted

import java.util.UUID

import redacted.accounts.{Account, StandardAccount}
import redacted.operations.{DepositOperation, WithdrawalOperation}
import redacted.transactions.{Transaction, TransactionLog}
import org.joda.time.DateTime

import scala.concurrent.stm.Ref

trait TestData {
  val now: DateTime = DateTime.parse("2019-02-06T08:20:00.000Z")

  val account1Id: UUID = UUID.fromString("4f85e447-65c8-4737-a60f-d60805a583a7")
  val account2Id: UUID = UUID.fromString("501c3872-40ef-458a-ab35-3465e6e87558")
  val account3Id: UUID = UUID.fromString("4986e95e-77bc-4efa-9b5b-2a8be0e9746f")
  val account4Id: UUID = UUID.fromString("c36f1ae5-c87a-4068-b761-52344a72ae00")

  val account1SavesALot: Account = StandardAccount(account1Id)
  val account2HasNoSavings: Account = StandardAccount(account2Id)
  val account3Regular: Account = StandardAccount(account3Id)
  val account4IsEmpty: Account = StandardAccount(account4Id)

  val account1Log: TransactionLog = {
    val transactions = Seq(
      Transaction(DepositOperation(amount = BigDecimal(50.00), account1SavesALot, "Some savings."), commitDate = now),
      Transaction(DepositOperation(amount = BigDecimal(100.00), account1SavesALot, "Again, some savings."), commitDate = now.minusDays(2)),
      Transaction(DepositOperation(amount = BigDecimal(50.00), account1SavesALot, "More savings."), commitDate = now.minusDays(3))
    )

    TransactionLog(transactions)
  }

  val account2Log: TransactionLog = {
    val transactions = Seq(
      Transaction(WithdrawalOperation(amount = BigDecimal(10.00), account = account2HasNoSavings, "Dinner with friends."), commitDate = now.minusDays(1)),
      Transaction(WithdrawalOperation(amount = BigDecimal(10.00), account = account2HasNoSavings, "Books."), commitDate = now.minusDays(2)),
      Transaction(WithdrawalOperation(amount = BigDecimal(10.00), account = account2HasNoSavings, "Food."), commitDate = now.minusDays(3)),
      Transaction(WithdrawalOperation(amount = BigDecimal(10.00), account = account2HasNoSavings, "More food."), commitDate = now.minusDays(4)),
      Transaction(DepositOperation(amount = BigDecimal(50.00), account = account2HasNoSavings, "Some savings."), commitDate = now.minusDays(5))
    )

    TransactionLog(transactions)
  }

  val account3Log: TransactionLog = {
    val transactions = Seq(
      Transaction(WithdrawalOperation(amount = BigDecimal(10.00), account = account3Regular, "Dinner with friends."), commitDate = now),
      Transaction(DepositOperation(amount = BigDecimal(100.00), account = account3Regular, "Some savings."), commitDate = now.minusDays(2))
    )

    TransactionLog(transactions)
  }

  private val account4Log = {
    TransactionLog(Seq.empty)
  }

  protected val AccountsRepository: Ref[Map[UUID, Account]] = Ref {
    Map(Seq(
      account1Id -> account1SavesALot,
      account2Id -> account2HasNoSavings,
      account3Id -> account3Regular,
      account4Id -> account4IsEmpty
    ): _*)
  }

  protected val LogsRepository: Ref[Map[UUID, TransactionLog]] = Ref {
    Map(Seq(
      account1Id -> account1Log,
      account2Id -> account2Log,
      account3Id -> account3Log,
      account4Id -> account4Log
    ): _*)
  }
}
