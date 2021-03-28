# Accounts service  
  
A service for managing monetary accounts 💵.  
  
### Requirements  
The project is written in Scala using the play framework, so you must have the Scala Build Tool (SBT) installed. You may install it on a MacOS using the brew package manager.

 `brew install sbt`
  
### Running  
You may run the application using SBT.

Run `sbt run`.
  
### Testing  
You may run tests using SBT.
Run `sbt test`.  
  
## REST API  
  
You may view complete documentation [here](https://documenter.getpostman.com/view/5599963/RztprTyg).  
  
### Account details  
`GET /accounts`  
  
`GET /accounts/:accountId`  
  
### Account balance  
`GET /accounts/:accountId/balance`  
  
`GET /accounts/:accountId/balance/details`  
  
### Account transactions  
`POST /accounts/:accountId/transactions/deposits`  
  
`POST /accounts/:accountId/transactions/withdrawals`  
  
## Implementation details  
  
All the business logic is located in the `redacted` package. This package is conformed of several sub packages. Each package defines its main interfaces in a Scala `package object`.

- `accounts` package: contains definitions related to monetary accounts. For example: `StandardAccount`, `StandardAccountType`, etc...
- `balances` package: contains definitions related to calculating balances.
- `operations` package: contains definitions related to monetary operations. Currently, the only implemented operations are deposit and withdrawal.
- `policies` package:  contains policies that are evaluated when trying to commit a monetary transaction, for example, the `HasEnoughFundsPolicy` validates that the account has enough funds when trying to withdraw money. You can see some examples on how to implement and use a policy in the `OperationEvaluator` test.
- `transactions` package: this package contains all the logic related to monetary transactions. It glues most components of the previous packages in order to commit a monetary transaction. It also the definitions for a transactions logs.
  
### Software Transactional Memory

In order to avoid concurrency issues, we leverage the power of an optimistic concurrency control mechanism called Software Transactional Memory (STM).  A high level explanation: when a thread tries to modify memory it creates a transaction, STM assumes that the content that the thread sees is not affected by any other threads. If the content of the memory changes then STM retries the transaction until it gets committed.

In code, I annotate the variables that I want to be STM enabled using a datatype called `Ref`. Then, when I want to modify that variable I wrap my code on an `atomic` block. The STM implementation homepage is located [here](https://nbronson.github.io/scala-stm/) (ScalaSTM).

### Scala persistent collections
We leverage Scala persistent collections (a.k.a immutable collections). When we update the transaction log of an account, we create a new collection out of the old one and place the new collection in the place of the old one. Of course, the reference that holds the transaction log can only be modified inside a ScalaSTM transaction.

#### How did I get to this solution?
At first, I thought I could solve the problem using the Actor model (the Akka actors framework is one of the strongest parts of Scala); basically an Actor encapsulates state and can only act on that state via messages that are queued on a message queue. Those semantics ensure that there is only one entity accessing the part of memory that the actor holds at the time.

Actors would solve the problem, but it was not a good use case since actors come in systems and hierarchies. Often, Actors are distributed on a network and are supposed to crash any time, and having only one singleton actor that holds all state is kind of an antipattern.
