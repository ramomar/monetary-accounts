import redacted.accounts.Account
import redacted.transactions.Transaction
import redacted.balances.{Balance, BalanceDetails}
import redacted.operations.{DepositOperation, Operation, WithdrawalOperation}
import org.joda.time.DateTime

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json.JodaWrites.jodaDateWrites
import play.api.libs.json.JodaReads.jodaDateReads
import play.api.libs.json.Reads.minLength

package object controllers {
  case class DepositRequest(amount: BigDecimal, concept: String)
  case class WithdrawalRequest(amount: BigDecimal, concept: String)

  implicit val dateWrites: Writes[DateTime] = jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val dateReads: Reads[DateTime] = jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val accountWrites: Writes[Account] = new Writes[Account] {
    def writes(account: Account): JsValue = Json.obj(
      "id" -> account.id.toString,
      "type" -> account.`type`.name
    )
  }

  implicit val operationWrites: Writes[Operation] = new Writes[Operation] {
    def writes(operation: Operation): JsValue =
      operation match {
        case DepositOperation(amount, account, concept) =>
          Json.obj(
            "type" -> operation.`type`.name,
            "amount" -> amount,
            "account" -> account,
            "concept" -> concept
          )
        case WithdrawalOperation(amount, account, concept) =>
          Json.obj(
            "type" -> operation.`type`.name,
            "amount" -> amount,
            "account" -> account,
            "concept" -> concept
          )
      }
  }

  implicit val transactionWrites: Writes[Transaction] = new Writes[Transaction] {
    def writes(transaction: Transaction): JsValue = Json.obj(
      "operation" -> transaction.operation
    ) + ("date" -> Json.toJson(transaction.commitDate))
  }

  implicit val balanceWrites: Writes[Balance] = new Writes[Balance] {
    def writes(balance: Balance): JsValue = Json.obj(
      "currentAmount" -> balance.currentAmount
    )
  }

  implicit val balanceDetailsWrites: Writes[BalanceDetails] = new Writes[BalanceDetails] {
    def writes(balance: BalanceDetails): JsValue = Json.obj(
      "currentAmount" -> balance.currentAmount,
      "transactions" -> Json.toJson(balance.log.transactions)
    )
  }

  implicit val depositRequestReads: Reads[DepositRequest] = (
    (JsPath \ "amount").read[BigDecimal] and
    (JsPath \ "concept").read[String](minLength[String](3))
    )(DepositRequest.apply _)

  implicit val withdrawalRequestReads: Reads[WithdrawalRequest] = (
    (JsPath \ "amount").read[BigDecimal] and
    (JsPath \ "concept").read[String](minLength[String](3))
    )(WithdrawalRequest.apply _)
}
