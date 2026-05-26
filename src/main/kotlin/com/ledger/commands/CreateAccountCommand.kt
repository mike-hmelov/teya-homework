package com.ledger.commands

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ledger.engine.BaseCommand
import jakarta.validation.constraints.NotEmpty

class CreateAccountCommand : BaseCommand() {
    @NotEmpty(message = "Name is required")
    lateinit var name: String

    @JsonIgnore
    var id: String? = null
}
