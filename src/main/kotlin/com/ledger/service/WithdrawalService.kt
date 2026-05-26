package com.ledger.service

import com.ledger.commands.CreateTransactionCommand
import com.ledger.model.Account
import com.ledger.model.Transaction
import com.ledger.model.TransactionDirection
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import java.util.*

@Service
class WithdrawalService {
    operator fun invoke(account: Account, command: CreateTransactionCommand): Mono<Transaction> {
        if (account.balance < command.amount) {
            return Mono.error { InsufficientFundsException("Not enough money") }
        }

        val trx = Transaction().apply {
            id = command.id ?: UUID.randomUUID().toString()
            accountId = account.id
            direction = TransactionDirection.OUT
            amount = command.amount
            timestamp = command.timestamp ?: OffsetDateTime.now()
        }
        account.balance -= trx.amount
        account.transactions.add(trx)
        return Mono.just(trx)
    }
}
