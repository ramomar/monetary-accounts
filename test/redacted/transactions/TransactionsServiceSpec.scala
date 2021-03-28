package redacted.transactions

import java.util.UUID

import redacted.{TestData, accounts}
import redacted.accounts.{Account, StandardAccountType}
import redacted.operations.{DepositOperation, OperationEvaluator, WithdrawalOperation}
import redacted.policies.HasEnoughBalanceWithdrawalPolicy

import org.scalatest.{FlatSpec, Matchers}

class TransactionsServiceSpec extends FlatSpec with Matchers {
  behavior of s"a ${classOf[TransactionsService]}"

  private object TransactionsTest extends TransactionsService with TestData

  private object Accounts extends TestData

  it should s"be able to persist transactions in memory." in {
    val deposit = DepositOperation(BigDecimal(50.00), Accounts.account4IsEmpty, "Savings.")
    val depositEvaluator = new OperationEvaluator[DepositOperation](Seq())
    val withdrawal = WithdrawalOperation(BigDecimal(50.00), Accounts.account4IsEmpty, "Dinner.")
    val withdrawalEvaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))

    TransactionsTest.findTransactions(Accounts.account4Id) match {
      case Some(TransactionLog(log)) => log shouldBe Seq.empty
      case None => fail(s"Was expecting a $TransactionLog")
    }

    TransactionsTest.performDeposit(deposit, depositEvaluator)

    TransactionsTest.findTransactions(Accounts.account4Id) match {
      case Some(TransactionLog(log)) => log.map(_.operation) shouldBe Seq(deposit)
      case None => fail(s"Was expecting a $TransactionLog")
    }

    TransactionsTest.performWithdrawal(withdrawal, withdrawalEvaluator)

    TransactionsTest.findTransactions(Accounts.account4Id) match {
      case Some(TransactionLog(log)) => log.map(_.operation) shouldBe Seq(withdrawal, deposit)
      case None => fail(s"Was expecting a $TransactionLog")
    }
  }

  it should "correctly commit a withdrawal in an account given some policies." in {
    val evaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))
    val operation = WithdrawalOperation(BigDecimal(50.00), Accounts.account1SavesALot, "Dinner.")
    val oldLog = TransactionsTest.findTransactions(Accounts.account1Id).get

    TransactionsTest.performWithdrawal(operation, evaluator) match {
      case SuccessfulTransactionProcessingResult(newLog) =>
        newLog.transactions.map(_.operation) shouldBe oldLog.addLog(operation).transactions.map(_.operation)
      case FailedTransactionProcessingResult(_) => fail(s"Was expecting a ${SuccessfulTransactionProcessingResult.getClass}")
    }
  }

  it should "correctly commit a deposit in an account given some policies." in {
    val evaluator = new OperationEvaluator[DepositOperation](Seq())
    val operation = DepositOperation(BigDecimal(50.00), Accounts.account1SavesALot, "Some savings.")
    val oldLog = TransactionsTest.findTransactions(Accounts.account1Id).get

    TransactionsTest.performDeposit(operation, evaluator) match {
      case SuccessfulTransactionProcessingResult(newLog) =>
        newLog.transactions.map(_.operation) shouldBe oldLog.addLog(operation).transactions.map(_.operation)
      case FailedTransactionProcessingResult(_) => fail(s"Was expecting a ${SuccessfulTransactionProcessingResult.getClass}")
    }
  }

  it should s"return a ${FailedTransactionProcessingResult.getClass} when there is an error with policies." in {
    val evaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))
    val operation = WithdrawalOperation(BigDecimal(50.00), Accounts.account2HasNoSavings, "Dinner.")

    TransactionsTest.performWithdrawal(operation, evaluator) shouldBe a [FailedTransactionProcessingResult]
  }

  it should s"return a ${FailedTransactionProcessingResult.getClass} when the user does not exists." in {
    val evaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))
    val nonExistentAccount = new Account {
      val `type`: accounts.AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(BigDecimal(50.00), nonExistentAccount, "Dinner.")

    TransactionsTest.performWithdrawal(operation, evaluator) shouldBe a [FailedTransactionProcessingResult]
  }

  it should s"return a ${FailedTransactionProcessingResult.getClass} with error messages." in {
    val evaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))
    val nonExistentAccount = new Account {
      val `type`: accounts.AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(BigDecimal(50.00), nonExistentAccount, "Dinner.")

    TransactionsTest.performWithdrawal(operation, evaluator) match {
      case FailedTransactionProcessingResult(reasons) =>
          reasons.head.message shouldBe "Account not found."
          reasons.head.code shouldBe "TXN-1"
      case _ => fail(s"Was expecting a ${FailedTransactionProcessingResult.getClass}")
    }
  }
}
