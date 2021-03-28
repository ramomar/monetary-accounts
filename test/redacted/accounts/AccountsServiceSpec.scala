package redacted.accounts

import redacted.TestData
import org.scalatest.{FlatSpec, Matchers}

class AccountsServiceSpec extends FlatSpec with Matchers {
  behavior of s"${classOf[AccountsService]}"

  private object Accounts extends AccountsService with TestData

  it should "return all the accounts." in {
    Accounts.all.length shouldBe 4
  }

  it should "return an account given an id." in {
    Accounts.findById(Accounts.account1Id) match {
      case Some(account) =>
        account shouldBe Accounts.account1SavesALot
      case None => fail(s"Was expecting an ${classOf[Account]}.")
    }
  }
}
