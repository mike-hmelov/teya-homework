package com.ledger.query

import com.ledger.dao.AccountRepository
import com.ledger.model.Account
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Component
class AccountQueries(private val accountRepository: AccountRepository) {
    fun findById(id: String): Mono<Account> {
        return accountRepository.findById(id)
            .switchIfEmpty { Mono.error { NoSuchElementException("Account with id $id not found") } }
    }
}