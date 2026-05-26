package com.ledger.transactions.model

import com.ledger.transactions.dto.TransactionDto
import java.math.BigDecimal
import java.time.OffsetDateTime

data class Transaction(
    val amount: BigDecimal,
    val direction: TransactionDirection,
    val created: OffsetDateTime
) {
    fun toDto(): TransactionDto {
        return TransactionDto(id, amount, direction, created)
    }

    lateinit var id: String
}

enum class TransactionDirection {
    IN,
    OUT
}