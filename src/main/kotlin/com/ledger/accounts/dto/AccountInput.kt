package com.ledger.accounts.dto

import com.ledger.accounts.model.Account
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class AccountInput(
    @NotNull
    val name: String
) {
    fun toDomain(): Account {
        return Account(name, BigDecimal.ZERO, mutableListOf())
    }
}
