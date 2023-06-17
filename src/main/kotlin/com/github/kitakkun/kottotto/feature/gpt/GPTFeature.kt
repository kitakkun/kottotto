package com.github.kitakkun.kottotto.feature.gpt

import com.github.kitakkun.kottotto.Config
import com.github.kitakkun.kottotto.feature.Feature
import com.hexadevlabs.gpt4all.LLModel
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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

    // to avoid duplicate regeneration
    private val regeneratingMessageIds = mutableListOf<Long>()

    init {
        val modelPath = Config.get("GPT4ALL_MODEL_PATH") ?: throw Exception("GPT4ALL_MODEL_PATH is not set.")
        model = LLModel(Path(modelPath))
        config = LLModel.config().build()
    }

    override fun register(jda: JDA) {
        jda.addEventListener(this)
    }

    private fun <T> InlineMessage<T>.generateEmbedContent(
        prompt: String,
        response: String,
        buttonText: String,
        buttonStyle: ButtonStyle,
    ) = embed {
        field(
            name = "Your prompt",
            value = prompt,
            inline = false,
        )
        field(
            name = "My answer",
            value = response,
            inline = false,
        )
        actionRow(
            button(id = "gpt_regenerate", label = buttonText, style = buttonStyle)
        )
        footer("Powered by gpt4all")
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != command.name) return
        if (event.member?.user?.isBot == true) return

        val prompt = event.options[0].asString

        event.interaction.deferReply().queue()
        try {
            val responseText = generateResponse(prompt) ?: throw Exception("Failed to generate response.")
            event.interaction.hook.sendMessage(
                MessageCreate {
                    generateEmbedContent(prompt, responseText, "Regenerate", ButtonStyle.PRIMARY)
                }
            ).queue()
        } catch (e: Exception) {
            event.interaction.hook.sendMessage(
                MessageCreate {
                    generateEmbedContent(prompt, "Internal error occurred.", "Retry", ButtonStyle.DANGER)
                }
            ).queue()
            e.printStackTrace()
        }
    }

    private fun generateResponse(prompt: String): String? {
        val response = model.generate(prompt, config)
        if (response.isBlank()) return null
        return response
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.button.id != "gpt_regenerate") return
        if (regeneratingMessageIds.contains(event.messageIdLong)) return
        val prompt = event.message.embeds[0].fields[0].value ?: return
        event.deferEdit().queue()
        regeneratingMessageIds.add(event.messageIdLong)
        val response = generateResponse(prompt)
        println(response)
        val message = MessageEdit {
            if (response != null) {
                generateEmbedContent(prompt, response, "Regenerate", ButtonStyle.PRIMARY)
            } else {
                generateEmbedContent(prompt, "Internal error occurred.", "Retry", ButtonStyle.DANGER)
            }
        }
        event.hook.editOriginal(message).queue()
        regeneratingMessageIds.remove(event.messageIdLong)
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.upsertCommand(command).queue()
    }
}
