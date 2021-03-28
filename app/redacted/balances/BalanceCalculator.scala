package redacted.balances

import redacted.operations.{DepositOperation, WithdrawalOperation}
import redacted.transactions.{Transaction, TransactionLog}

object BalanceCalculator {
  def computeBalance(transactionsLog: TransactionLog): BigDecimal =
    transactionsLog.transactions.foldLeft(BigDecimal(0)) { case (acc, Transaction(operation, _)) =>
      operation match {
        case DepositOperation(amount, _, _) =>
          acc + amount
        case WithdrawalOperation(amount, _, _) =>
          acc - amount
      }
    }
}
