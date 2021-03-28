package modules

import com.google.inject.AbstractModule
import redacted.transactions.TransactionsServiceLike

class TransactionsServiceLikeModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[TransactionsServiceLike])
      .to(classOf[Transactions])
  }
}