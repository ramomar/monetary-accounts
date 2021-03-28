package redacted.balances

import java.util.UUID

import redacted.transactions.TransactionLog

import scala.concurrent.stm.Ref

trait BalancesService extends BalancesServiceLike {
  protected val LogsRepository: Ref[Map[UUID, TransactionLog]]

  def getBalanceForAccount(id: UUID): Option[Balance] =
    LogsRepository.single()
      .get(id)
      .map(BalanceCalculator.computeBalance)
      .map(Balance.apply)

  def getBalanceDetails(id: UUID): Option[BalanceDetails] =
    LogsRepository.single()
      .get(id).map { logs =>
      val currentAmount = BalanceCalculator.computeBalance(logs)
      BalanceDetails(currentAmount, logs)
    }
}
