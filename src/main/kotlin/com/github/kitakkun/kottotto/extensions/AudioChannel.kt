package com.github.kitakkun.kottotto.extensions

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel

fun AudioChannel.join() = guild.audioManager.openAudioConnection(this)
fun AudioChannel.leave() = guild.audioManager.closeAudioConnection()