package redacted

import java.util.UUID

import redacted.transactions.TransactionLog

package object balances {
  case class Balance(currentAmount: BigDecimal)
  case class BalanceDetails(currentAmount: BigDecimal, log: TransactionLog)

  trait BalancesServiceLike {
    def getBalanceForAccount(id: UUID): Option[Balance]
    def getBalanceDetails(id: UUID): Option[BalanceDetails]
  }
}
