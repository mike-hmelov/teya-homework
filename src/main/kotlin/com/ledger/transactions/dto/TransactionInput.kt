package com.ledger.transactions.dto

import com.ledger.transactions.model.TransactionDirection
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class TransactionInput(
    @NotNull
    val direction: TransactionDirection,
    @NotNull
    @Positive
    val amount: BigDecimal
)