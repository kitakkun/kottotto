package com.github.kitakkun.kottotto.lavaplayer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    private val player: AudioPlayer
): AudioEventAdapter() {

    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    private fun nextTrack() {
        this.player.startTrack(this.queue.poll(), false)
    }

    fun queue(track: AudioTrack) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track)
        }
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        if (endReason?.mayStartNext == true) {
            nextTrack()
        }
    }
}
