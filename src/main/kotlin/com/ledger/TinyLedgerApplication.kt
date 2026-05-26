package com.ledger

import com.ledger.accounts.dao.AccountsRepository
import com.ledger.accounts.model.Account
import com.ledger.transactions.dao.TransactionsRepository
import com.ledger.transactions.model.Transaction
import com.ledger.transactions.model.TransactionDirection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootApplication
class TinyLedgerApplication

fun main(args: Array<String>) {
    runApplication<TinyLedgerApplication>(*args)
}

@Component
class Initializer : CommandLineRunner {
    @Autowired
    lateinit var accountsRepository: AccountsRepository
    @Autowired
    lateinit var transactionsRepository: TransactionsRepository

    override fun run(vararg args: String) {
        fun acc(id: String, a: Account) {
            accountsRepository.save(id,a)
        }

        fun trx(id: String, t: Transaction): Transaction {
            transactionsRepository.save(id, t)
            return t
        }

        acc(
            "580e3b02-02f3-4146-98e6-92f1a8eca786",
            Account(
                "John Dow", BigDecimal(1000), mutableListOf(
                    trx(
                        "580e3b02-02f3-4146-98e6-92f1a8eca785",
                        Transaction(
                            BigDecimal(1000), TransactionDirection.IN,
                            OffsetDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                        )
                    )
                )
            )
        )


        acc(
            "591bbbab-a984-4cb1-bb4f-643657617949",
            Account(
                "Mary Jane", BigDecimal(1000), mutableListOf(
                    trx(
                        "591bbbab-a984-4cb1-bb4f-643657617948", Transaction(
                            BigDecimal(500),
                            TransactionDirection.IN,
                            OffsetDateTime.of(1986, 4, 26, 1, 23, 45, 0, ZoneOffset.of("+2"))
                        )
                    ),
                    trx(
                        "591bbbab-a984-4cb1-bb4f-643657617947",
                        Transaction(
                            BigDecimal(750),
                            TransactionDirection.IN,
                            OffsetDateTime.of(2011, 3, 11, 14, 46, 0, 0, ZoneOffset.of("+9"))
                        )
                    ),
                    trx(
                        "591bbbab-a984-4cb1-bb4f-643657617946",
                        Transaction(
                            BigDecimal(250),
                            TransactionDirection.OUT,
                            OffsetDateTime.of(2026, 5, 1, 10, 11, 12, 0, ZoneOffset.of("+2"))
                        )
                    )
                )
            )
        )
    }
}