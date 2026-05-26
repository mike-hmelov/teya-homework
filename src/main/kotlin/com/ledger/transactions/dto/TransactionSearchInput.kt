package com.ledger.transactions.dto

import com.ledger.transactions.model.TransactionDirection
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class TransactionSearchInput(
    val direction: TransactionDirection? = null,
    @Min(0)
    val minAmount: BigDecimal? = null,
    @Positive
    val maxAmount: BigDecimal? = null,
    @Min(1)
    @Max(1000)
    val limit: Long = 100,

    @Min(0)
    val offset: Long = 0
)