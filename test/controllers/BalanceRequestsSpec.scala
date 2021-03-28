package controllers

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BalanceRequestsSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "BalancesController GET /accounts/:accountId/balance" should {
    "respond with a json that contains the account balance." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accountId = (contentAsJson(accountsResponse) \ "body" \ 0 \ "id").as[String]

      val depositRequest = FakeRequest(POST, s"/accounts/$accountId/transactions/deposits")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 200.00,
            |	"concept": "Some savings."
            |}
          """.stripMargin)
        )

      val depositResponse = Await.result(route(app, depositRequest).get, Duration(1, TimeUnit.SECONDS))

      val balanceResponse = route(app, FakeRequest(GET, s"/accounts/$accountId/balance")).get

      val expectedBody =
        """
          |{
          |    "code": "success",
          |    "body": {
          |        "currentAmount": 200.00
          |    }
          |}
        """.stripMargin

      status(balanceResponse) mustBe OK
      contentType(balanceResponse) mustBe Some("application/json")
      contentAsJson(balanceResponse) mustBe Json.parse(expectedBody)
    }

    "respond with status code 404 when the account does not exists and a proper response body." in {
      val accountId = "00000000-1111-2222-3333-444444444444"

      val balanceResponse = route(app, FakeRequest(GET, s"/accounts/$accountId/balance")).get

      val expectedBody =
        """
          |{
          |    "code": "account_not_found",
          |    "message": "Account not found."
          |}
        """.stripMargin

      status(balanceResponse) mustBe NOT_FOUND
      contentType(balanceResponse) mustBe Some("application/json")
      contentAsJson(balanceResponse) mustBe Json.parse(expectedBody)
    }
  }

  "BalancesController GET /accounts/:accountId/balance/details" should {
    "respond with a json that contains the account balance details." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accountId = (contentAsJson(accountsResponse) \ "body" \ 1 \ "id").as[String]

      val depositRequest = FakeRequest(POST, s"/accounts/$accountId/transactions/deposits")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 200.00,
            |	"concept": "Some savings."
            |}
          """.stripMargin)
        )

      val depositResponse = Await.result(route(app, depositRequest).get, Duration(1, TimeUnit.SECONDS))

      val response = route(app, FakeRequest(GET, s"/accounts/$accountId/balance/details")).get

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val payload = contentAsJson(response)
      (payload \ "code").as[String] mustBe "success"
      (payload \ "body" \ "currentAmount").as[BigDecimal] mustBe BigDecimal(200.00)
      (payload \ "body" \ "transactions" \ 0 \ "operation" \ "type").as[String] mustBe "deposit"
      (payload \ "body" \ "transactions" \ 0 \ "operation" \ "amount").as[BigDecimal] mustBe BigDecimal(200.00)
      (payload \ "body" \"transactions" \ 0 \ "operation" \ "concept").as[String] mustBe "Some savings."
      (payload \ "body" \ "transactions" \ 0 \ "operation" \ "account" \ "id").as[String] mustBe accountId
    }

    "respond with status code 404 when the account does not exists and a proper response body." in {
      val accountId = UUID.fromString("00000000-1111-2222-3333-444444444444")

      val response = route(app, FakeRequest(GET, s"/accounts/${accountId.toString}")).get

      val expectedBody =
        """
          |{
          |    "code": "account_not_found",
          |    "message": "Account not found."
          |}
        """.stripMargin

      status(response) mustBe NOT_FOUND
      contentType(response) mustBe Some("application/json")
      contentAsJson(response) mustBe Json.parse(expectedBody)
    }
  }
}
