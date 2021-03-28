package modules

import redacted.Data
import redacted.accounts.AccountsService
import redacted.balances.BalancesService
import redacted.transactions.TransactionsService

private[modules] class Transactions extends TransactionsService with Data
private[modules] class Balances extends BalancesService with Data
private[modules] class Accounts extends AccountsService with Data
