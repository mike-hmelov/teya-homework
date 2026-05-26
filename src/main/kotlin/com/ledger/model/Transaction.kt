package com.ledger.model

import com.ledger.dto.TransactionDto
import java.math.BigDecimal
import java.time.OffsetDateTime

class Transaction {
    lateinit var id: String
    lateinit var accountId: String
    lateinit var direction: TransactionDirection
    lateinit var amount: BigDecimal
    lateinit var timestamp: OffsetDateTime

    fun toDto(): TransactionDto {
        return TransactionDto(amount, direction, timestamp)
    }
}

enum class TransactionDirection {
    IN,
    OUT
}