package com.ledger.engine

class NoSuchHandlerException(klass: Class<out BaseCommand>) :
    RuntimeException("No handler for command ${klass.simpleName}")
