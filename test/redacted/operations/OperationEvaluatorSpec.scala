package redacted.operations

import java.util.UUID

import redacted.balances.Balance
import redacted.accounts.{Account, AccountType, StandardAccountType}
import redacted.policies._
import org.scalatest.{FlatSpec, Matchers}

class OperationEvaluatorSpec extends FlatSpec with Matchers {
  type O <: Operation

  behavior of s"${classOf[OperationEvaluator[O]]}"

  it should s"return a ${CompliantOperationResult.getClass} when the operation complies with a specified policy." in {
    info(s"${HasEnoughBalanceWithdrawalTestPolicy.getClass}: operation involves withdrawing less or equal money than the available balance.")

    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(amount = BigDecimal(100.00), account = account, concept="Dinner with friends")
    val balance = Balance(currentAmount = BigDecimal(150.00))

    val policies = Seq(HasEnoughBalanceWithdrawalPolicy)

    val operationEvaluator = new OperationEvaluator[WithdrawalOperation](policies)

    operationEvaluator.evaluate(operation, balance) shouldBe CompliantOperationResult
  }

  it should s"return a ${NonCompliantOperationResult.getClass} when the operation complies with a specified policy." in {
    info(s"$HasEnoughBalanceWithdrawalTestPolicy: operation involves withdrawing more money than the available balance.")

    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }
    val operation = WithdrawalOperation(amount = BigDecimal(200.00), account, concept = "Dinner with friends")
    val balance = Balance(currentAmount = BigDecimal(150.00))

    val policies = Seq(HasEnoughBalanceWithdrawalPolicy)

    val operationEvaluator = new OperationEvaluator[WithdrawalOperation](policies)

    operationEvaluator.evaluate(operation, balance) shouldBe a [NonCompliantOperationResult]
  }

  it should s"return a ${CompliantOperationResult.getClass} when the operation complies with all the specified redacted.policies." in {
    info(s"${OnlyWithdrawEvenQuantitiesTestPolicy.getClass}: withdraw amount is an even number.")
    info(s"${NotGreaterThanOneHundredTestPolicy.getClass}: withdraw amount is not greater than one hundred.")

    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }

    val operation = WithdrawalOperation(amount = BigDecimal(50.00), account, concept = "Dinner with friends")
    val balance = Balance(currentAmount = BigDecimal(200.00))

    val policies = Seq(
      OnlyWithdrawEvenQuantitiesTestPolicy,
      NotGreaterThanOneHundredTestPolicy
    )

    val operationEvaluator = new OperationEvaluator[WithdrawalOperation](policies)

    operationEvaluator.evaluate(operation, balance) shouldBe CompliantOperationResult
  }

  it should s"return a $NonCompliantOperationResult when the operation does not comply with all the specified redacted.policies, as well as a summary of reasons for being a non compliant operation." in {
    info(s"${HasEnoughBalanceWithdrawalTestPolicy.getClass}: operation involves withdrawing less or equal money than the available balance.")
    info(s"${OnlyWithdrawEvenQuantitiesTestPolicy.getClass}: withdraw amount is an even number.")
    info(s"${NotGreaterThanOneHundredTestPolicy.getClass}: withdraw amount is not greater than one hundred.")

    val account: Account = new Account {
      val `type`: AccountType = StandardAccountType
      val id: UUID = UUID.randomUUID()
    }

    val operation = WithdrawalOperation(amount = BigDecimal(101.00), account, concept = "Dinner with friends")
    val balance = Balance(currentAmount = BigDecimal(50.00))

    val policies = Seq(
      NotGreaterThanOneHundredTestPolicy,
      OnlyWithdrawEvenQuantitiesTestPolicy,
      HasEnoughBalanceWithdrawalTestPolicy,
    )

    val operationEvaluator = new OperationEvaluator[WithdrawalOperation](policies)

    operationEvaluator.evaluate(operation, balance) match {
      case NonCompliantOperationResult(reasons) => reasons shouldBe Seq(NotEnoughFundsReason, NotEvenNumberReason, GreaterThanOneHundredReason)
      case CompliantOperationResult => fail(s"Was expecting a $NonCompliantOperationResult")
    }
  }

  final object NotEnoughFundsReason extends PolicyEvaluationNonCompliantReason("Not enough funds.", "TEST-1")

  final object NotEvenNumberReason extends PolicyEvaluationNonCompliantReason("Amount to withdraw is not an even number.", "TEST-2")

  final object GreaterThanOneHundredReason extends PolicyEvaluationNonCompliantReason("Amount to withdraw is greater than one hundred.", "TEST-3")

  private object HasEnoughBalanceWithdrawalTestPolicy extends WithdrawalOperationPolicy {
    val name = "has_enough_balance_withdrawal_test_policy"

    def evaluate(operation: WithdrawalOperation, balance: Balance): PolicyEvaluationResult = {
      val amount = operation.amount
      val currentAmount = balance.currentAmount

      if (amount <= currentAmount) PolicyEvaluationCompliantResult
      else PolicyEvaluationNonCompliantResult(NotEnoughFundsReason)
    }
  }

  private object OnlyWithdrawEvenQuantitiesTestPolicy extends WithdrawalOperationPolicy {
    val name: String = "only_withdraw_even_quantities_test_policy"

    def evaluate(operation: WithdrawalOperation, balance: Balance): PolicyEvaluationResult =
      if (operation.amount % 2 == 0) PolicyEvaluationCompliantResult
      else PolicyEvaluationNonCompliantResult(NotEvenNumberReason)
  }

  private object NotGreaterThanOneHundredTestPolicy extends WithdrawalOperationPolicy {
    val name: String = "not_greater_than_one_hundred_policy_test_policy"

    def evaluate(operation: WithdrawalOperation, accountState: Balance): PolicyEvaluationResult =
      if (operation.amount < 100) PolicyEvaluationCompliantResult
      else PolicyEvaluationNonCompliantResult(GreaterThanOneHundredReason)
  }
}
