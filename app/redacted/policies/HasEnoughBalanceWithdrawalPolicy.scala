package redacted.policies

import redacted.balances.Balance
import redacted.operations.WithdrawalOperation

object HasEnoughBalanceWithdrawalPolicy extends WithdrawalOperationPolicy {
  val name = "has_enough_balance_withdrawal_policy"

  def evaluate(operation: WithdrawalOperation, balance: Balance): PolicyEvaluationResult = {
    val amount = operation.amount
    val currentAmount = balance.currentAmount

    if (amount <= currentAmount) PolicyEvaluationCompliantResult
    else PolicyEvaluationNonCompliantResult(NotEnoughFundsReason)
  }
}
