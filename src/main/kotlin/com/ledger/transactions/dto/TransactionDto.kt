package com.ledger.transactions.dto

import com.ledger.transactions.model.TransactionDirection
import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionDto(
    val id: String,
    val amount: BigDecimal,
    val direction: TransactionDirection,
    val created: OffsetDateTime
)