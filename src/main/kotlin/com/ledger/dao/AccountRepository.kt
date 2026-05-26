package com.ledger.dao

import com.ledger.model.Account
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class AccountRepository {
    private val accounts = ConcurrentHashMap<String, Account>()

    fun findById(id: String): Mono<Account> {
        return Mono.justOrEmpty(accounts[id])
    }

    fun countByName(name: String): Int {
        return accounts.values.count { it.name == name }
    }

    fun save(id: String?, account: Account): Mono<Account> {
        account.id = id ?: UUID.randomUUID().toString()
        accounts[account.id] = account
        return Mono.just(account)
    }
}