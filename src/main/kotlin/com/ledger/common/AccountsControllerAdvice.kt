package com.ledger.common

import com.ledger.accounts.service.AccountAlreadyExistsException
import com.ledger.transactions.service.InsufficientFundsException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.NoSuchElementException

@RestControllerAdvice
class AccountsControllerAdvice {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(AccountAlreadyExistsException::class)
    fun handleConflict(ex: AccountAlreadyExistsException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Account with this name already exists")
    }

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(ex: InsufficientFundsException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_REQUIRED, ex.message ?: "Insufficient funds")
    }
}