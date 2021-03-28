package redacted.policies

import java.util.UUID

import redacted.accounts.{Account, AccountType, StandardAccountType}
import redacted.balances.Balance
import org.scalatest.{FlatSpec, Matchers}
import redacted.operations.{WithdrawalOperation, WithdrawalOperationType}

class HasEnoughBalanceWithdrawalPolicySpec extends FlatSpec with Matchers {
  behavior of s"$HasEnoughBalanceWithdrawalPolicy"

  it should s"return a $PolicyEvaluationCompliantResult when the amount to withdrawal is less or equal than the current account balance." in {

    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(amount = BigDecimal(50.00), account, concept = "Dinner")
    val balance = Balance(currentAmount = BigDecimal(150.00))

    HasEnoughBalanceWithdrawalPolicy.evaluate(operation, balance) shouldBe PolicyEvaluationCompliantResult
  }

  it should s"return a ${PolicyEvaluationNonCompliantResult.getClass} when the amount to withdrawal is more than the current balance." in {
    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(amount = BigDecimal(150.00), account, concept = "Dinner")
    val balance = Balance(currentAmount = BigDecimal(100.00))

    HasEnoughBalanceWithdrawalPolicy.evaluate(operation, balance) shouldBe a [PolicyEvaluationNonCompliantResult]
  }

  it should s"return a ${NotEnoughFundsReason.getClass} with the correct message and code when policy evaluation is non compliant." in {
    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(amount = BigDecimal(150.00), account, concept = "Dinner")
    val balance = Balance(currentAmount = BigDecimal(100.00))

    HasEnoughBalanceWithdrawalPolicy.evaluate(operation, balance) match {
      case PolicyEvaluationNonCompliantResult(reason) =>
        reason match {
          case r: NotEnoughFundsReason.type =>
            r.code shouldBe "WDL-1"
            r.message shouldBe "Not enough funds."
          case _ => fail(s"Was expecting a $NotEnoughFundsReason.")
        }
      case _ => fail(s"Was expecting a $PolicyEvaluationNonCompliantResult.")
    }
  }

  it should "have a correct name." in {
    HasEnoughBalanceWithdrawalPolicy.name shouldBe "has_enough_balance_withdrawal_policy"
  }

  it should "have a correct operation type." in {
    HasEnoughBalanceWithdrawalPolicy.operationType shouldBe WithdrawalOperationType
  }
}
