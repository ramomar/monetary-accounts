package redacted.transactions

import java.util.UUID

import redacted.balances.{Balance, BalanceCalculator}
import redacted.operations._
import redacted.operations.OperationEvaluator

import scala.concurrent.stm._

trait TransactionsService extends TransactionsServiceLike {
  protected val LogsRepository: Ref[Map[UUID, TransactionLog]]

  def findTransactions(id: UUID): Option[TransactionLog] = {
    val logs = LogsRepository.single()
    logs.get(id)
  }

  def performWithdrawal(operation: WithdrawalOperation, evaluator: OperationEvaluator[WithdrawalOperation]): TransactionProcessingResult = {
    val accountExists = checkIfAccountExists(operation.account.id)

    if (accountExists) processWithdrawalOperation(operation, evaluator)
    else FailedTransactionProcessingResult(Seq(AccountNotFound))
  }

  def performDeposit(operation: DepositOperation, evaluator: OperationEvaluator[DepositOperation]): TransactionProcessingResult = {
    val accountExists = checkIfAccountExists(operation.account.id)

    if (accountExists) processDepositOperation(operation, evaluator)
    else FailedTransactionProcessingResult(Seq(AccountNotFound))
  }

  private def processWithdrawalOperation(operation: WithdrawalOperation, evaluator: OperationEvaluator[WithdrawalOperation]): TransactionProcessingResult = atomic { implicit txn =>
    val logId = operation.account.id
    val accountsLogs = LogsRepository()
    val log = accountsLogs(logId)
    val currentAmount = BalanceCalculator.computeBalance(log)

    evaluator.evaluate(operation, Balance(currentAmount)) match {
      case CompliantOperationResult =>
        LogsRepository() = accountsLogs.updated(logId, log.addLog(operation))
        val updatedLogs = LogsRepository()
        val withdrawalResult = updatedLogs(logId)
        SuccessfulTransactionProcessingResult(withdrawalResult)
      case NonCompliantOperationResult(reasons) =>
        FailedTransactionProcessingResult(reasons.map(r => FailedTransactionReason(r.message, r.code)))
    }
  }

  private def processDepositOperation(operation: DepositOperation, evaluator: OperationEvaluator[DepositOperation]): TransactionProcessingResult = atomic { implicit txn =>
    val logId = operation.account.id
    val accountsLogs = LogsRepository()
    val log = accountsLogs(logId)
    val currentAmount = BalanceCalculator.computeBalance(log)

    evaluator.evaluate(operation, Balance(currentAmount)) match {
      case CompliantOperationResult =>
          val logId = operation.account.id
          LogsRepository() = accountsLogs.updated(logId, log.addLog(operation))
          val updatedLogs = LogsRepository()
          val depositResult = updatedLogs(logId)
        SuccessfulTransactionProcessingResult(depositResult)
      case NonCompliantOperationResult(reasons) =>
        FailedTransactionProcessingResult(reasons.map(r => FailedTransactionReason(r.message, r.code)))
    }
  }

  private def checkIfAccountExists(accountId: UUID): Boolean =
    LogsRepository.single().contains(accountId)
}
