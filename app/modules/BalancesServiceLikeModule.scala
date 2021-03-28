package modules

import com.google.inject.AbstractModule
import redacted.balances.BalancesServiceLike

class BalancesServiceLikeModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[BalancesServiceLike])
      .to(classOf[Balances])
  }
}
