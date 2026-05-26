package com.ledger.accounts.dto

import java.math.BigDecimal

data class AccountDto(val id: String, val name: String, val balance: BigDecimal)
