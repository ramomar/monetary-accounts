package controllers


import java.util.concurrent.TimeUnit

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TransactionsRequestsSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {
  "POST /accounts/:accountId/transactions/deposits" should {
    "respond with a json that contains the result of successfully processing a deposit." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accountId = (contentAsJson(accountsResponse) \ "body" \ 0 \ "id").as[String]

      val request = FakeRequest(POST, s"/accounts/$accountId/transactions/deposits")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00,
            |	"concept": "Some savings."
            |}
          """.stripMargin)
        )

      val response = route(app, request).get
      status(response) mustBe CREATED
      contentType(response) mustBe Some("application/json")

      val responseBody = contentAsJson(response)
      (responseBody \ "code").as[String] mustBe "success"
      (responseBody \ "body" \ "operation" \ "type").as[String] mustBe "deposit"
      (responseBody \ "body" \ "operation" \ "amount").as[BigDecimal] mustBe 40
      (responseBody \ "body" \ "operation" \ "concept").as[String] mustBe "Some savings."
    }

    "respond with a json that contains the result of trying to issue a deposit with an incomplete payload." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val account = (contentAsJson(accountsResponse) \ "body").as[Seq[JsObject]].head
      val accountId = (account \ "id").as[String]

      val request = FakeRequest(POST, s"/accounts/$accountId/transactions/deposits")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00
            |}
          """.stripMargin))

      val response = route(app, request).get
      status(response) mustBe BAD_REQUEST
      contentType(response) mustBe Some("application/json")

      val responseBody = contentAsJson(response)
      (responseBody \ "code").as[String] mustBe "client_error"
    }
  }

  "POST /accounts/:accountId/transactions/withdrawals" should {
    "respond with a json that contains the result of successfully processing a withdrawal (the account has enough funds)." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accountId = (contentAsJson(accountsResponse) \ "body" \ 0 \ "id").as[String]

      val depositRequest = FakeRequest(POST, s"/accounts/$accountId/transactions/deposits")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00,
            |	"concept": "Some savings."
            |}
          """.stripMargin)
        )

      val depositResponse = Await.result(route(app, depositRequest).get, Duration.apply(1, TimeUnit.SECONDS))

      val request = FakeRequest(POST, s"/accounts/$accountId/transactions/withdrawals")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00,
            |	"concept": "Dinner."
            |}
          """.stripMargin)
        )

      val response = route(app, request).get

      status(response) mustBe CREATED
      contentType(response) mustBe Some("application/json")

      val responseBody = contentAsJson(response)
      (responseBody \ "code").as[String] mustBe "success"
      (responseBody \ "body" \ "operation" \ "type").as[String] mustBe "withdrawal"
      (responseBody \ "body" \ "operation" \ "amount").as[BigDecimal] mustBe 40
      (responseBody \ "body" \ "operation" \ "concept").as[String] mustBe "Dinner."
    }

    "respond with a json that contains the result of failing to process a withdrawal (the account has no funds)." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accounts: Seq[JsObject] = (contentAsJson(accountsResponse) \ "body").as[Seq[JsObject]]
      val account = accounts(2)
      val accountId = (account \ "id").as[String]

      val request = FakeRequest(POST, s"/accounts/$accountId/transactions/withdrawals")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00,
            |	"concept": "Dinner."
            |}
          """.stripMargin)
        )

      val response = route(app, request).get

      status(response) mustBe CONFLICT
      contentType(response) mustBe Some("application/json")

      val responseBody = contentAsJson(response)
      (responseBody \ "code").as[String] mustBe "could_not_process_transaction"
      (responseBody \ "reasons" \ 0 \ "code").as[String] mustBe "WDL-1"
      (responseBody \ "reasons" \ 0 \ "message").as[String] mustBe "Not enough funds."
    }

    "respond with a json that contains the result of trying to issue a withdrawal with an incomplete payload." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val account = (contentAsJson(accountsResponse) \ "body").as[Seq[JsObject]].head
      val accountId = (account \ "id").as[String]

      val request = FakeRequest(POST, s"/accounts/$accountId/transactions/withdrawals")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(Json.parse(
          """
            |{
            |	"amount": 40.00
            |}
          """.stripMargin))

      val response = route(app, request).get

      status(response) mustBe BAD_REQUEST
      contentType(response) mustBe Some("application/json")

      val responseBody = contentAsJson(response)
      (responseBody \ "code").as[String] mustBe "client_error"
    }
  }
}
