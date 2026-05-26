package com.ledger.accounts.service

import com.ledger.accounts.dao.AccountsRepository
import com.ledger.accounts.dto.AccountDto
import com.ledger.accounts.dto.AccountInput
import org.springframework.stereotype.Service

@Service
class AccountsService(val accountsRepository: AccountsRepository) {

    fun get(id: String): AccountDto {
        return accountsRepository.findById(id)?.toDto() ?: throw NoSuchElementException("Not such account")
    }

    fun create(input: AccountInput): AccountDto {
        if (accountsRepository.existsByName(input.name)) {
            throw AccountAlreadyExistsException()
        }
        return accountsRepository.save(input.toDomain()).toDto()
    }
}
