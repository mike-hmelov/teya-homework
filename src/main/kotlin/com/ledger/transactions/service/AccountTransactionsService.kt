package com.ledger.transactions.service

import com.ledger.accounts.dao.AccountsRepository
import com.ledger.accounts.model.Account
import com.ledger.transactions.dao.TransactionsRepository
import com.ledger.transactions.dto.TransactionDto
import com.ledger.transactions.dto.TransactionInput
import com.ledger.transactions.model.Transaction
import com.ledger.transactions.model.TransactionDirection
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class AccountTransactionsService(
    val accountsRepository: AccountsRepository,
    val transactionsRepository: TransactionsRepository
) {
    fun get(id: String): List<TransactionDto> {
        val account = accountsRepository.findById(id) ?: throw NoSuchElementException("No such account")
        return account.transactions.map { it.toDto() }
    }

    fun processTransaction(accountId: String, tx: TransactionInput): TransactionDto {
        val account = accountsRepository.findById(accountId) ?: throw NoSuchElementException("No such account")
        return when (tx.direction) {
            TransactionDirection.IN -> topUpMoney(account, tx)
            TransactionDirection.OUT -> withdrawMoney(account, tx)
        }
    }

    private fun topUpMoney(
        account: Account,
        tx: TransactionInput
    ): TransactionDto {
        val transaction = transactionsRepository.save(Transaction(tx.amount, tx.direction, OffsetDateTime.now()))
        account.transactions.add(transaction)
        account.balance += tx.amount
        return transaction.toDto()
    }

    private fun withdrawMoney(
        account: Account,
        tx: TransactionInput
    ): TransactionDto {
        if (account.balance < tx.amount) throw InsufficientFundsException()
        val transaction = transactionsRepository.save(Transaction(tx.amount, tx.direction, OffsetDateTime.now()))
        account.balance -= tx.amount
        account.transactions.add(transaction)
        return transaction.toDto()
    }
}
