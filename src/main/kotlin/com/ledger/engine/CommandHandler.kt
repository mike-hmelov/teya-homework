package com.ledger.engine

import reactor.core.publisher.Mono

interface CommandHandler<IN : BaseCommand, OUT : Any> {
    fun commandType(): Class<IN>
    fun handle(command: IN): Mono<OUT> {
        return Mono.empty()
    }
}