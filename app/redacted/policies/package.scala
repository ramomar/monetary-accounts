package redacted

import redacted.balances.Balance
import redacted.operations._

package object policies {
  trait OperationPolicy[O <: Operation] {
    val operationType: OperationType

    val name: String

    def evaluate(operation: O, balance: Balance): PolicyEvaluationResult
  }

  trait WithdrawalOperationPolicy extends OperationPolicy[WithdrawalOperation] {
    val operationType: OperationType = WithdrawalOperationType
  }

  trait DepositOperationPolicy extends OperationPolicy[DepositOperation] {
    val operationType: OperationType = DepositOperationType
  }

  sealed trait PolicyEvaluationResult

  case object PolicyEvaluationCompliantResult extends PolicyEvaluationResult

  case class PolicyEvaluationNonCompliantResult(reason: PolicyEvaluationNonCompliantReason) extends PolicyEvaluationResult

  case class PolicyEvaluationNonCompliantReason(message: String, code: String) {
    override def toString: String =
      s"$code - $message"
  }

  final object NotEnoughFundsReason extends PolicyEvaluationNonCompliantReason("Not enough funds.", "WDL-1")
}
