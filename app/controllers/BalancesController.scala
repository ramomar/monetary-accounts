package controllers

import java.util.UUID

import javax.inject._
import redacted.balances.BalancesServiceLike
import play.api.mvc._
import play.api.libs.json._

@Singleton
class BalancesController @Inject()(cc: ControllerComponents,
                                   balancesService: BalancesServiceLike)
  extends AbstractController(cc) {
  def balance(accountId: UUID) = Action { implicit request: Request[AnyContent] =>
    balancesService.getBalanceForAccount(accountId) match {
      case Some(balance) => Ok(Responses.successResponse(Json.toJson(balance)))
      case None => NotFound(Responses.accountNotFoundResponse)
    }
  }

  def balanceDetails(accountId: UUID) = Action { implicit request: Request[AnyContent] =>
    balancesService.getBalanceDetails(accountId) match {
      case Some(balanceDetails) => Ok(Responses.successResponse(Json.toJson(balanceDetails)))
      case None => NotFound(Responses.accountNotFoundResponse)
    }
  }
}
