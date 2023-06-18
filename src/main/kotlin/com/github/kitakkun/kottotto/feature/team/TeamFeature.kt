package com.github.kitakkun.kottotto.feature.team

import com.github.kitakkun.kottotto.extensions.emitEmbeds
import com.github.kitakkun.kottotto.feature.Feature
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

class TeamFeature : Feature, ListenerAdapter() {
    companion object {
        private const val buttonExpirationSeconds: Long = 30
    }

    private val command = Command(
        name = "team",
        description = "sort out members in voice channel to teams"
    ) {
        addOptions(
            OptionData(
                OptionType.INTEGER,
                "members_per_team",
                "number of members per team",
                true,
            )
        )
    }

    override fun register(jda: JDA) {
        jda.guilds.forEach {
            it.upsertCommand(command).queue()
        }
        jda.addEventListener(this)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != command.name) return

        val voiceChannel = event.member?.voiceState?.channel
        if (voiceChannel == null) {
            event.reply("You need to join a voice channel first.").queue()
            return
        }

        val members = voiceChannel.members
        if (members.size < 2) {
            event.reply("There are not enough members in the voice channel. At least 2 members are required.").queue()
            return
        }

        val memberCount = event.getOption("members_per_team")?.asInt ?: return
        if (members.size < memberCount) {
            event.reply("You cannot divide members into teams more than the number of members.").queue()
            return
        }

        val shuffledMembers = members.shuffled()
        val teams = shuffledMembers.chunked(memberCount)
        event.reply(MessageCreate {
            generateContent(
                teams.map {
                    it.map { member -> member.asMention }
                }
            )
        }).queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != "team_regenerate") return

        if (disableButtonIfExpired(event)) return

        // number of fields equals to the number of teams
        val fields = event.message.embeds.flatMap { it.fields }
        val teamCount = fields.size

        // all members' mentions in the message
        val members = event.message.embeds.flatMap { it.fields.map { it.value } }.filterNotNull()
            .filterNot { it.isBlank() }

        // do nothing if the user is not in the message
        if (!members.contains(event.user.asMention)) return

        // calculate backward the number of members per team
        val oneTeamMemberCount = (members.size + 1) / teamCount

        val shuffledMembers = members.shuffled()
        val teams = shuffledMembers.chunked(oneTeamMemberCount)
        event.editMessage(MessageEdit { generateContent(teams) }).queue()
    }

    private fun <T> InlineMessage<T>.generateContent(
        teams: List<List<String>>,
    ) {
        embed {
            teams.map {
                field(
                    name = "Team ${teams.indexOf(it) + 1}",
                    value = it.joinToString("\n"),
                    inline = true,
                )
            }
            actionRow(
                button(
                    id = "team_regenerate",
                    label = "Retry",
                    style = ButtonStyle.PRIMARY,
                )
            )
        }
    }

    private fun disableButtonIfExpired(event: ButtonInteractionEvent): Boolean {
        val buttonClickedTime = event.timeCreated.toEpochSecond()
        val messageCreatedTime = event.message.timeCreated.toEpochSecond()
        if (buttonClickedTime - messageCreatedTime > buttonExpirationSeconds) {
            event.editMessage(
                MessageEdit {
                    emitEmbeds(event.message.embeds)
                    embed {
                        description = "You cannot re-generate teams after $buttonExpirationSeconds seconds.\n" +
                                "Please re-run the command to generate new teams."
                    }
                    actionRow(
                        event.button.asDisabled()
                            .withLabel("Expired")
                            .withStyle(ButtonStyle.DANGER)
                    )
                }
            ).queue()
            return true
        }
        return false
    }

}