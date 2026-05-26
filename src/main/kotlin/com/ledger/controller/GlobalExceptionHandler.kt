package com.ledger.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import com.ledger.service.InsufficientFundsException
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@RestControllerAdvice
    class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to (ex.message ?: "Resource not found")))
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to (ex.message ?: "Invalid state")))
        )
    }

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(ex: InsufficientFundsException): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(mapOf("error" to (ex.message ?: "Insufficient funds")))
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(ex: WebExchangeBindException): Mono<ResponseEntity<Map<String, Any>>> {
        val errors = ex.bindingResult.fieldErrors.map {
            mapOf("field" to it.field, "message" to (it.defaultMessage ?: "Invalid value"))
        }
        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "validation_error", "details" to errors))
        )
    }
}