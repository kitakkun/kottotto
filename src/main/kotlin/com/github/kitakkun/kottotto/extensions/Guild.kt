package com.github.kitakkun.kottotto.extensions

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.Category

fun Guild.getCategoryByChannelId(channelId: Long): Category? = categories.find { it.channels.map { it.idLong }.contains(channelId) }
