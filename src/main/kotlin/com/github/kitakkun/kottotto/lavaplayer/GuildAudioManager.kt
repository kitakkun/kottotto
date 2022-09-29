package com.github.kitakkun.kottotto.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildAudioManager constructor(
    private val manager: AudioPlayerManager
) {
    private val audioPlayer: AudioPlayer
    val scheduler: TrackScheduler
    val sendHandler: AudioPlayerSendHandler

    init {
        this.audioPlayer = manager.createPlayer()
        this.scheduler = TrackScheduler(this.audioPlayer)
        this.audioPlayer.addListener(this.scheduler)
        this.sendHandler = AudioPlayerSendHandler(this.audioPlayer)
    }
}
