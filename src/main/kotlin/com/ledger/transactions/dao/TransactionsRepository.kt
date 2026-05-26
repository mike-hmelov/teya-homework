package com.ledger.transactions.dao

import com.ledger.transactions.model.Transaction
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TransactionsRepository {
    private val transactions = mutableMapOf<String, Transaction>()

    fun findById(id: String): Transaction? {
        return transactions[id]
    }

    fun save(transaction: Transaction): Transaction {
        return save(UUID.randomUUID().toString(), transaction)
    }

    fun save(id: String, transaction: Transaction): Transaction {
        transaction.id = id
        transactions[transaction.id] = transaction
        return transaction
    }

    fun findAll(): List<Transaction> = transactions.values.toList()

}
