package com.ledger.model

import com.ledger.dto.AccountDto
import java.math.BigDecimal

class Account {
    lateinit var id: String
    lateinit var name: String
    lateinit var balance: BigDecimal
    lateinit var transactions: MutableList<Transaction>

    fun toDto(): AccountDto {
        return AccountDto(id, name, balance, transactions.map { it.toDto() })
    }
}