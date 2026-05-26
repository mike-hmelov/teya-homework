package com.ledger.engine

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CommandProcessor(val logger: CommandLogger, handlerList: List<CommandHandler<*, *>>) {
    final val handlers: Map<Class<out BaseCommand>, CommandHandler<*, *>> =
        handlerList.associateBy { it.commandType() }

    fun <IN : BaseCommand, OUT : Any> handle(command: IN): Mono<OUT> {
        return logger.save(command)
            .flatMap { doHandle(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <IN : BaseCommand, OUT : Any> doHandle(command: IN): Mono<OUT> {
        val handler = handlers[command.javaClass] as CommandHandler<IN, OUT>?
        return handler?.handle(command)
            ?: Mono.error { NoSuchHandlerException(command.javaClass) }
    }
}