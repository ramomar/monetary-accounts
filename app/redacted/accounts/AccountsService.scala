package redacted.accounts

import java.util.UUID

import scala.concurrent.stm.Ref

trait AccountsService extends AccountsServiceLike {
  protected val AccountsRepository: Ref[Map[UUID, Account]]

  def all: Seq[Account] =
    AccountsRepository.single()
      .valuesIterator
      .toSeq

  def findById(id: UUID): Option[Account] =
    AccountsRepository.single()
      .get(id)
}
