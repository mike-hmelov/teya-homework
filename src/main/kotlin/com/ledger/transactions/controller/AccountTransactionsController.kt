package com.ledger.transactions.controller

import com.ledger.transactions.dto.TransactionDto
import com.ledger.transactions.dto.TransactionInput
import com.ledger.transactions.service.AccountTransactionsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts/{id}/transactions")
class AccountTransactionsController(val accountTransactionsService: AccountTransactionsService) {
    @GetMapping
    fun getAccountTransactions(@PathVariable("id") id: String): ResponseEntity<List<TransactionDto>> {
        return accountTransactionsService.get(id).let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun processTransaction(
        @PathVariable("id") id: String,
        @Validated @RequestBody tx: TransactionInput
    ): ResponseEntity<TransactionDto> {
        return accountTransactionsService.processTransaction(id, tx).let {
            ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/transactions/${it.id}")
                .body(it)
        }
    }
}