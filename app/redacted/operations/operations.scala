package redacted

import redacted.accounts.Account
import redacted.policies.PolicyEvaluationNonCompliantReason

package object operations {
  sealed trait OperationType {
    val name: String
  }

  case object DepositOperationType extends OperationType {
    val name = "deposit"
  }

  case object WithdrawalOperationType extends OperationType {
    val name = "withdrawal"
  }

  sealed trait Operation {
    val `type`: OperationType
    val account: Account
    val concept: String
  }

  case class DepositOperation(amount: BigDecimal, account: Account, concept: String) extends Operation {
    val `type`: OperationType = DepositOperationType

    override def toString: String =
      s"operation=${`type`.name} amount=$amount account=$account concept=$concept"
  }

  case class WithdrawalOperation(amount: BigDecimal, account: Account, concept: String) extends Operation {
    val `type`: OperationType = WithdrawalOperationType

    override def toString: String =
      s"operation=${`type`.name} amount=$amount account=$account concept=$concept"
  }

  sealed trait OperationEvaluationResult

  case object CompliantOperationResult extends OperationEvaluationResult

  case class NonCompliantOperationResult(reasons: Seq[PolicyEvaluationNonCompliantReason]) extends OperationEvaluationResult
}
