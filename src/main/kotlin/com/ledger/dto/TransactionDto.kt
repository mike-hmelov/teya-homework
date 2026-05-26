package com.ledger.dto

import com.ledger.model.TransactionDirection
import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionDto(val amount: BigDecimal, val direction: TransactionDirection, val timestamp: OffsetDateTime)