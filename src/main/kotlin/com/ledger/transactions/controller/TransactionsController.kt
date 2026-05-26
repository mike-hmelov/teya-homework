package com.ledger.transactions.controller

import com.ledger.transactions.dto.TransactionDto
import com.ledger.transactions.dto.TransactionSearchInput
import com.ledger.transactions.service.TransactionsService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionsController(val transactionsService: TransactionsService) {
    @GetMapping("{id}")
    fun getTransaction(@PathVariable("id") id: String): ResponseEntity<TransactionDto> {
        return transactionsService.get(id).let { ResponseEntity.ok(it) }
    }

    @PostMapping("/search")
    fun searchTransactions(@Validated @RequestBody searchInput: TransactionSearchInput): ResponseEntity<List<TransactionDto>> {
        return transactionsService.searchTransactions(searchInput).let { ResponseEntity.ok(it) }
    }
}