package controllers

import java.util.UUID

import javax.inject._
import redacted.accounts.AccountsServiceLike
import redacted.operations.{DepositOperation, OperationEvaluator, WithdrawalOperation}
import redacted.policies.HasEnoughBalanceWithdrawalPolicy
import redacted.transactions.{FailedTransactionProcessingResult, SuccessfulTransactionProcessingResult, TransactionsServiceLike}
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class TransactionsController @Inject()(cc: ControllerComponents,
                                       accountsService: AccountsServiceLike,
                                       transactionsService: TransactionsServiceLike)
  extends AbstractController(cc) {
  def performDeposit(accountId: UUID) = Action(parse.json[DepositRequest]) { implicit request: Request[DepositRequest] =>
    val DepositRequest(amount, concept) = request.body

    accountsService.findById(accountId) match {
      case Some(account) =>
        val operation = DepositOperation(amount, account, concept)
        val evaluator = new OperationEvaluator[DepositOperation](Seq.empty)

        transactionsService.performDeposit(operation, evaluator) match {
          case SuccessfulTransactionProcessingResult(log) => Created(Responses.successResponse(Json.toJson(log.transactions.head)))
          case FailedTransactionProcessingResult(reasons) => Conflict(Responses.transactionConflict(reasons))
        }
      case None =>
        NotFound(Responses.accountNotFoundResponse)
    }
  }

  def performWithdrawal(accountId: UUID) = Action(parse.json[WithdrawalRequest]) {
    implicit request: Request[WithdrawalRequest] =>
      val WithdrawalRequest(amount, concept) = request.body

      accountsService.findById(accountId) match {
        case Some(account) =>
          val operation = WithdrawalOperation(amount, account, concept)
          val evaluator = new OperationEvaluator[WithdrawalOperation](Seq(HasEnoughBalanceWithdrawalPolicy))

          transactionsService.performWithdrawal(operation, evaluator) match {
            case SuccessfulTransactionProcessingResult(log) => Created(Responses.successResponse(Json.toJson(log.transactions.head)))
            case FailedTransactionProcessingResult(reasons) => Conflict(Responses.transactionConflict(reasons))
          }
        case None =>
          NotFound(Responses.accountNotFoundResponse)
      }
  }
}
