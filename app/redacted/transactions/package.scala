package redacted

import redacted.operations.{DepositOperation, Operation, OperationEvaluator, WithdrawalOperation}
import org.joda.time.DateTime

package object transactions {

  case class TransactionLog(transactions: Seq[Transaction]) {
    def addLog(operation: Operation): TransactionLog =
      copy(transactions = Transaction(operation, DateTime.now) +: transactions)
  }

  case object TransactionLog {
    def empty: TransactionLog =
      TransactionLog(Seq.empty)
  }

  case class Transaction(operation: Operation, commitDate: DateTime)

  sealed trait TransactionProcessingResult

  case class SuccessfulTransactionProcessingResult(log: TransactionLog) extends TransactionProcessingResult

  case class FailedTransactionProcessingResult(reasons: Seq[FailedTransactionReason]) extends TransactionProcessingResult

  case class FailedTransactionReason(message: String, code: String) {
    override def toString: String =
      s"$code $message"
  }

  object AccountNotFound extends FailedTransactionReason("Account not found.", "TXN-1")

  trait TransactionsServiceLike {
    def performWithdrawal(operation: WithdrawalOperation, evaluator: OperationEvaluator[WithdrawalOperation]): TransactionProcessingResult
    def performDeposit(operation: DepositOperation, evaluator: OperationEvaluator[DepositOperation]): TransactionProcessingResult
  }
}
