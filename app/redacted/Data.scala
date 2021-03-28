package redacted

import java.util.UUID

import redacted.transactions.TransactionLog
import accounts.{Account, StandardAccount}

import scala.concurrent.stm.Ref

trait Data {
  protected val AccountsRepository: Ref[Map[UUID, Account]] = DataGenerator.AccountsRepository
  protected val LogsRepository: Ref[Map[UUID, TransactionLog]] = DataGenerator.LogsRepository
}

private object DataGenerator {
  val accounts: Map[UUID, Account] = makeAccounts(5)

  val AccountsRepository: Ref[Map[UUID, Account]] =
    Ref(DataGenerator.accounts)

  val LogsRepository: Ref[Map[UUID, TransactionLog]] =
    Ref(AccountsRepository.single().mapValues(_ => TransactionLog.empty))

  def makeAccounts(n: Int): Map[UUID, Account] = {
    val uuidAndAccounts = Seq.fill(n) {
      val uuid = UUID.randomUUID()
      val account = StandardAccount(uuid)
      (uuid, StandardAccount(uuid))
    }

    Map(uuidAndAccounts: _*)
  }
}