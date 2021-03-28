package redacted.operations

import redacted.balances.Balance
import redacted.policies.{OperationPolicy, PolicyEvaluationCompliantResult, PolicyEvaluationNonCompliantReason, PolicyEvaluationNonCompliantResult}

class OperationEvaluator[O <: Operation](policies: Seq[OperationPolicy[O]]) {

  def evaluate(operation: O, balance: Balance): OperationEvaluationResult = {
    val (compliantEvaluations, nonCompliantEvaluationsReasons) = policies
      .foldLeft((Seq.empty[OperationPolicy[O]], Seq.empty[PolicyEvaluationNonCompliantReason])) { (acc, policy: OperationPolicy[O]) =>
        val (compliantEvaluations, nonCompliantEvaluationsReasons) = acc

        policy.evaluate(operation, balance) match {
          case PolicyEvaluationCompliantResult => (policy +: compliantEvaluations, nonCompliantEvaluationsReasons)
          case PolicyEvaluationNonCompliantResult(reason) => (compliantEvaluations, reason +: nonCompliantEvaluationsReasons)
        }
      }

    if (nonCompliantEvaluationsReasons.isEmpty) CompliantOperationResult
    else NonCompliantOperationResult(nonCompliantEvaluationsReasons)
  }
}
