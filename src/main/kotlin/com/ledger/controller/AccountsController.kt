package com.ledger.controller

import com.ledger.commands.CreateAccountCommand
import com.ledger.commands.CreateTransactionCommand
import com.ledger.dto.AccountDto
import com.ledger.dto.TransactionDto
import com.ledger.engine.CommandProcessor
import com.ledger.query.AccountQueries
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/accounts")
class AccountsController(
    private val accountQueries: AccountQueries,
    private val commandProcessor: CommandProcessor
) {

    @GetMapping("/{id}")
    fun getAccount(@PathVariable("id") id: String): Mono<AccountDto> {
        return accountQueries.findById(id)
            .map { it.toDto() }
    }

    @GetMapping("/{id}/transactions")
    fun getTransactions(@PathVariable("id") id: String): Flux<TransactionDto> {
        return accountQueries.findById(id)
            .flatMapMany { Flux.fromIterable(it.transactions) }
            .map { it.toDto() }
    }

    @PostMapping
    fun createAccount(@Validated @RequestBody createAccountCommand: CreateAccountCommand): Mono<ResponseEntity<AccountDto>> {
        return commandProcessor.handle<CreateAccountCommand, AccountDto>(createAccountCommand)
            .map {
                ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.LOCATION, "/accounts/${it.id}")
                    .body(it)
            }
    }

    @PostMapping("{id}/transactions")
    fun processTransaction(
        @PathVariable("id") id: String,
        @Validated @RequestBody createTransactionCommand: CreateTransactionCommand
    ): Mono<TransactionDto> {
        createTransactionCommand.accountId = id
        return commandProcessor.handle(createTransactionCommand)
    }
}