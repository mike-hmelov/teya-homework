package com.ledger.transactions.service

import com.ledger.transactions.dao.TransactionsRepository
import com.ledger.transactions.dto.TransactionDto
import com.ledger.transactions.dto.TransactionSearchInput
import com.ledger.transactions.model.Transaction
import org.springframework.stereotype.Service
import java.util.function.Predicate

@Service
class TransactionsService(val transactionsRepository: TransactionsRepository) {
    fun get(id: String): TransactionDto {
        return transactionsRepository.findById(id)?.toDto() ?: throw NoSuchElementException("No such transaction")
    }

    fun searchTransactions(searchInput: TransactionSearchInput): List<TransactionDto> {
        return transactionsRepository.findAll()
            .stream()
            .filter(makeFilter(searchInput))
            .skip(searchInput.offset)
            .limit(searchInput.limit)
            .map { it.toDto() }
            .toList()
    }

    private fun makeFilter(searchInput: TransactionSearchInput): Predicate<Transaction> {
        var filter: Predicate<Transaction> = Predicate { true }

        if (searchInput.direction != null) {
            filter = filter.and { tr: Transaction -> tr.direction == searchInput.direction }
        }
        if (searchInput.minAmount != null) {
            filter = filter.and { tr: Transaction -> tr.amount >= searchInput.minAmount }
        }
        if (searchInput.maxAmount != null) {
            filter = filter.and { tr: Transaction -> tr.amount <= searchInput.maxAmount }
        }
        return filter
    }
}
