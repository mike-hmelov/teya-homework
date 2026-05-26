package com.ledger.dto

import java.math.BigDecimal

data class AccountDto(val id: String, val name: String, val balance: BigDecimal, val transactions: List<TransactionDto>)