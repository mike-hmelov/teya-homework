package com.ledger.commands

import com.ledger.dao.AccountRepository
import com.ledger.dto.TransactionDto
import com.ledger.engine.CommandHandler
import com.ledger.model.Account
import com.ledger.model.Transaction
import com.ledger.model.TransactionDirection
import com.ledger.service.TopUpService
import com.ledger.service.WithdrawalService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class CreateTransactionHandler(
    val accountRepository: AccountRepository,
    val topUpService: TopUpService,
    val withdrawalService: WithdrawalService
) : CommandHandler<CreateTransactionCommand, TransactionDto> {


    override fun commandType() = CreateTransactionCommand::class.java

    override fun handle(command: CreateTransactionCommand): Mono<TransactionDto> {
        return accountRepository.findById(command.accountId)
            .switchIfEmpty { Mono.error { NoSuchElementException("Account with id ${command.accountId} not found") } }
            .flatMap {
                handleOperation(it, command)
            }
            .map {
                it.toDto()
            }
    }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    private fun handleOperation(account: Account, command: CreateTransactionCommand): Mono<Transaction> {
        return when (command.direction) {
            TransactionDirection.IN -> topUpService(account, command)
            TransactionDirection.OUT -> withdrawalService(account, command)
            else -> Mono.error { IllegalArgumentException("Invalid transaction direction") }
        }
    }
}