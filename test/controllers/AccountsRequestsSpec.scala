package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsObject, Json}
import play.api.test._
import play.api.test.Helpers._

class AccountsRequestsSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /accounts" should {
    "respond with a json that contains all of the accounts." in {
      val response = route(app, FakeRequest(GET, "/accounts")).get

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")

      val payload = contentAsJson(response)
      (payload \ "code").as[String] mustBe "success"
      (payload \ "body").as[Seq[JsObject]].foreach { accountJson =>
        assert((accountJson \ "id").asOpt[String].isDefined)
        assert((accountJson \ "type").asOpt[String].isDefined)
      }
    }
  }

  "GET /account/:accountId" should {
    "respond with a json that contains the account details." in {
      val accountsResponse = route(app, FakeRequest(GET, "/accounts")).get
      val accountId = (contentAsJson(accountsResponse) \ "body" \ 0 \ "id").as[String]
      val accountType = (contentAsJson(accountsResponse) \ "body" \ 0 \ "type").as[String]
      val response = route(app, FakeRequest(GET, s"/accounts/$accountId")).get

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")

      val payload = contentAsJson(response)

      (payload \ "code").as[String] mustBe "success"
      (payload \ "body" \ "id").as[String] mustBe accountId
      (payload \ "body" \ "type").as[String] mustBe accountType
    }

    "respond with status code 404 when the account does not exists and a proper response body." in {
      val accountId = "00000000-1111-2222-3333-444444444444"
      val response = route(app, FakeRequest(GET, s"/accounts/$accountId")).get

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
