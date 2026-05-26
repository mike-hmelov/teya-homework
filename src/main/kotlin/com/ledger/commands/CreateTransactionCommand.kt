package com.ledger.commands

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ledger.engine.BaseCommand
import com.ledger.model.TransactionDirection
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.OffsetDateTime

class CreateTransactionCommand : BaseCommand() {
    lateinit var accountId: String

    @NotNull
    lateinit var direction: TransactionDirection

    @NotNull
    @Positive
    lateinit var amount: BigDecimal

    //prevent properties to set from outside
    @JsonIgnore
    var id: String? = null

    @JsonIgnore
    var timestamp: OffsetDateTime? = null
}