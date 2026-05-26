package com.ledger.commands

import com.ledger.dao.AccountRepository
import com.ledger.dto.AccountDto
import com.ledger.engine.CommandHandler
import com.ledger.model.Account
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class CreateAccountHandler(val accountRepository: AccountRepository) :
    CommandHandler<CreateAccountCommand, AccountDto> {

    override fun commandType(): Class<CreateAccountCommand> = CreateAccountCommand::class.java

    override fun handle(command: CreateAccountCommand): Mono<AccountDto> {
        val count = accountRepository.countByName(command.name)
        return if (count == 0) {
            val account = Account().apply {
                name = command.name
                balance = BigDecimal.ZERO
                transactions = mutableListOf()
            }
            accountRepository.save(command.id, account)
                .map { it.toDto() }
        } else
            Mono.error { IllegalStateException("Account with name ${command.name} already exists") }
    }
}