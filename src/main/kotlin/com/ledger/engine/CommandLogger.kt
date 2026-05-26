package com.ledger.engine

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class CommandLogger {
    val commandLog = ConcurrentLinkedQueue<BaseCommand>()
    fun <IN : BaseCommand> save(command: IN): Mono<IN> {
        commandLog.add(command)
        return Mono.just(command)
    }
}