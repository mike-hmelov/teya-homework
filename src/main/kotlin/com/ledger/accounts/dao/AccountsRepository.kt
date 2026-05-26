package com.ledger.accounts.dao

import com.ledger.accounts.model.Account
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AccountsRepository {
    private val accounts = mutableMapOf<String, Account>()

    fun findById(id: String): Account? = accounts[id]

    fun save(account: Account): Account {
        return save(UUID.randomUUID().toString(), account)
    }

    fun save(id: String, account: Account): Account {
        account.id = id
        accounts[account.id] = account
        return account
    }

    fun existsByName(name: String): Boolean {
        return accounts.values.map { it.name }.contains(name)
    }
}