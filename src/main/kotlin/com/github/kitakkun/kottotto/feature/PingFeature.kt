package com.github.kitakkun.kottotto.feature

import dev.minn.jda.ktx.interactions.commands.Command
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PingFeature : Feature, ListenerAdapter() {
    val command = Command(
        name = "ping",
        description = "replies with pong!",
    )

    override fun register(jda: JDA) {
        jda.upsertCommand(command).queue()
        jda.addEventListener(this)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != command.name) return
        event.interaction.reply("pong!").queue()
    }

}
