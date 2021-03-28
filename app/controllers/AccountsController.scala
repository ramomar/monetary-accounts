package controllers

import java.util.UUID

import javax.inject._
import redacted.accounts.AccountsServiceLike
import play.api.mvc._
import play.api.libs.json._

@Singleton
class AccountsController @Inject()(cc: ControllerComponents,
                                   accountsService: AccountsServiceLike) extends AbstractController(cc) {
  def accounts = Action { implicit request: Request[AnyContent] =>
    Ok(Responses.successResponse(Json.toJson(accountsService.all)))
  }

  def account(accountId: UUID) = Action { implicit request: Request[AnyContent] =>
    accountsService.findById(accountId) match {
      case Some(account) => Ok(Responses.successResponse(Json.toJson(account)))
      case None => NotFound(Responses.accountNotFoundResponse)
    }
  }
}
