package com.github.kitakkun.kottotto.feature

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on

suspend fun setupPingCommand(kord: Kord) {
    val pingCommand = kord.createGlobalChatInputCommand(
        name = "ping",
        description = "replies with pong!",
    )

    kord.on<ChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()
        if (interaction.command.rootId != pingCommand.id) return@on
        response.respond {
            content = "pong!"
        }
    }
}
