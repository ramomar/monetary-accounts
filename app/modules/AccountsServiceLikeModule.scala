package modules

import com.google.inject.AbstractModule
import redacted.accounts.AccountsServiceLike

class AccountsServiceLikeModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AccountsServiceLike])
      .to(classOf[Accounts])
  }
}
