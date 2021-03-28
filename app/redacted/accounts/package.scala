package redacted

import java.util.UUID

package object accounts {
  sealed trait AccountType {
    val name: String
  }

  case object StandardAccountType extends AccountType {
    val name = "standard"

    override def toString: String =
      name
  }

  trait Account {
    val `type`: AccountType
    val id: UUID

    override def toString: String =
    s"type=${`type`} account_id=$id"
  }

  case class StandardAccount(id: UUID) extends Account {
    val `type`: AccountType = StandardAccountType
  }

  trait AccountsServiceLike {
    def all: Seq[Account]
    def findById(uuid: UUID): Option[Account]
  }
}
