package com.github.kitakkun.kottotto.feature.gpt

import com.github.kitakkun.kottotto.Config
import com.github.kitakkun.kottotto.feature.Feature
import com.hexadevlabs.gpt4all.LLModel
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.io.path.Path

class GPTFeature : Feature, ListenerAdapter() {
    private val command = Command(
        name = "gpt",
        description = "chat with gpt4all."
    ) {
        addOptions(
            OptionData(
                OptionType.STRING,
                "prompt",
                "prompt to gpt4all.",
                true,
            )
        )
    }

    private val model: LLModel
    private val config: LLModel.GenerationConfig

    init {
        val modelPath = Config.get("GPT4ALL_MODEL_PATH") ?: throw Exception("GPT4ALL_MODEL_PATH is not set.")
        model = LLModel(Path(modelPath))
        config = LLModel.config().build()
    }

    override fun register(jda: JDA) {
        jda.addEventListener(this)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != command.name) return
        if (event.member?.user?.isBot == true) return

        val prompt = event.options[0].asString

        event.interaction.deferReply().queue()
        try {
            val responseText = model.generate(prompt, config)
            if (responseText.isBlank()) {
                throw Exception("response is blank.")
            }
            event.interaction.hook.sendMessageEmbeds(
                Embed {
                    field(
                        name = "Your prompt",
                        value = prompt,
                        inline = false,
                    )
                    field(
                        name = "My answer",
                        value = responseText,
                        inline = false,
                    )
                    footer("Powered by gpt4all")
                }
            ).queue()
        } catch (e: Exception) {
            event.hook.sendMessageEmbeds(
                Embed {
                    field(
                        name = "Your prompt",
                        value = prompt,
                        inline = false,
                    )
                    field(
                        name = "My answer",
                        value = "Internal error occurred.",
                        inline = false,
                    )
                }
            ).queue()
            e.printStackTrace()
        }
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.upsertCommand(command).queue()
    }
}
