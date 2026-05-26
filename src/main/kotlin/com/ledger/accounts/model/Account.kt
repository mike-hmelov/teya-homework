package com.ledger.accounts.model

import com.ledger.accounts.dto.AccountDto
import com.ledger.transactions.model.Transaction
import java.math.BigDecimal

class Account(
    val name: String,
    var balance: BigDecimal,
    val transactions: MutableList<Transaction>
) {
    lateinit var id: String


    fun toDto(): AccountDto {
        return AccountDto(id, name, balance)
    }
}