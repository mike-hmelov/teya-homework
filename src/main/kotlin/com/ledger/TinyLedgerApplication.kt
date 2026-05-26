package com.ledger

import com.ledger.dao.AccountRepository
import com.ledger.model.Account
import com.ledger.model.Transaction
import com.ledger.model.TransactionDirection
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
    lateinit var accountsRepository: AccountRepository


    override fun run(vararg args: String) {
        fun acc(id: String, a: Account) {
            accountsRepository.save(id, a)
        }

        acc(
            "580e3b02-02f3-4146-98e6-92f1a8eca786",
            Account().apply {
                name = "John Dow"
                balance = BigDecimal(1000)
                transactions = mutableListOf(
                    Transaction().apply {
                        id = "580e3b02-02f3-4146-98e6-92f1a8eca785"
                        accountId = "580e3b02-02f3-4146-98e6-92f1a8eca786"
                        amount = BigDecimal(1000)
                        direction = TransactionDirection.IN
                        timestamp = OffsetDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                    }
                )
            }
        )


        acc(
            "591bbbab-a984-4cb1-bb4f-643657617949",
            Account().apply {
                name = "Mary Jane"
                balance = BigDecimal(1000)
                transactions = mutableListOf(
                    Transaction().apply {
                        id = "591bbbab-a984-4cb1-bb4f-643657617948"
                        accountId = "591bbbab-a984-4cb1-bb4f-643657617949"
                        amount = BigDecimal(500)
                        direction = TransactionDirection.IN
                        timestamp = OffsetDateTime.of(1986, 4, 26, 1, 23, 45, 0, ZoneOffset.of("+2"))
                    },
                    Transaction().apply {
                        id = "591bbbab-a984-4cb1-bb4f-643657617947"
                        accountId = "591bbbab-a984-4cb1-bb4f-643657617949"
                        amount = BigDecimal(750)
                        direction = TransactionDirection.IN
                        timestamp = OffsetDateTime.of(2011, 3, 11, 14, 46, 0, 0, ZoneOffset.of("+9"))

                    },
                    Transaction().apply {
                        id = "591bbbab-a984-4cb1-bb4f-643657617946"
                        accountId = "591bbbab-a984-4cb1-bb4f-643657617949"
                        amount = BigDecimal(250)
                        direction = TransactionDirection.OUT
                        timestamp = OffsetDateTime.of(2026, 5, 1, 10, 11, 12, 0, ZoneOffset.of("+2"))
                    }
                )
            }
        )
    }
}