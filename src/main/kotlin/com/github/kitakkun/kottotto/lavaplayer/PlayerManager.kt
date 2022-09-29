package com.github.kitakkun.kottotto.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild

object PlayerManager {

    private val audioManagers = HashMap<Long, GuildAudioManager>()
    private val localPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val remotePlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    private fun audioLoadResultHandler(audioManager: GuildAudioManager) =
        object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                track?.let { audioManager.scheduler.queue(it) }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
            }

            override fun noMatches() {
            }

            override fun loadFailed(exception: FriendlyException?) {
            }
        }

    init {
        AudioSourceManagers.registerLocalSource(localPlayerManager)
        AudioSourceManagers.registerRemoteSources(remotePlayerManager)
    }

    fun getAudioManager(guild: Guild): GuildAudioManager {
        return audioManagers.computeIfAbsent(guild.idLong) {
            val audioManager = GuildAudioManager(localPlayerManager)
            guild.audioManager.sendingHandler = audioManager.sendHandler
            return@computeIfAbsent audioManager
        }
    }

    fun loadRemoteSourceAndPlay(guild: Guild, url: String) {
        val audioManager = getAudioManager(guild)
        remotePlayerManager.loadItem(url, audioLoadResultHandler(audioManager))

    }

    fun loadLocalSourceAndPlay(guild: Guild, url: String) {
        val audioManager = getAudioManager(guild)
        localPlayerManager.loadItem(url, audioLoadResultHandler(audioManager))
    }
}
