package com.ledger.accounts.controller

import com.ledger.accounts.dto.AccountDto
import com.ledger.accounts.dto.AccountInput
import com.ledger.accounts.service.AccountsService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/accounts")
class AccountsController(val accountsService: AccountsService) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: String): ResponseEntity<AccountDto> {
        return accountsService.get(id).let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun create(@Validated @RequestBody input: AccountInput): ResponseEntity<AccountDto> {
        return accountsService.create(input).let {
            ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/accounts/${it.id}")
                .body(it)
        }
    }
}