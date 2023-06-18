package com.github.kitakkun.kottotto.extensions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.MessageEmbed

fun <T> InlineMessage<T>.emitEmbeds(
    embeds: List<MessageEmbed>,
) {
    this.embeds += embeds
}