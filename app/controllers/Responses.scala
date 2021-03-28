package controllers

import redacted.transactions.FailedTransactionReason
import play.api.libs.json._

object Responses {
  def successResponse(body: JsValue): JsValue = Json.obj(
    "code" -> "success",
    "body" -> body
  )

  def accountNotFoundResponse: JsValue = Json.obj(
    "code" -> "account_not_found",
    "message" -> "Account not found."
  )

  def transactionConflict(reasons: Seq[FailedTransactionReason]): JsValue = Json.obj(
    "code" -> "could_not_process_transaction",
    "message" -> "There was a problem processing the transaction",
    "reasons" -> reasons.map { case FailedTransactionReason(message, code) =>
        Json.obj(
          "code" -> code,
          "message" -> message
        )
    }
  )

  def clientErrorResponse(message: String): JsValue = Json.obj(
    "code" -> "client_error",
    "message" -> message
  )

  def serverErrorResponse(message: String): JsValue = Json.obj(
    "code" -> "server_error",
    "message" -> message
  )
}
