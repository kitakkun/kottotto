package com.github.kitakkun.kottotto.soundengine.voicevox

import com.github.kitakkun.kottotto.soundengine.SoundEngine
import com.github.kitakkun.ktvox.api.KtVoxApi
import java.io.File

class VoiceVoxEngine(
    private val ktVoxApi: KtVoxApi,
) : SoundEngine {
    override suspend fun generateSoundFileFromText(
        inputTextFile: File,
        outputSoundFile: File,
    ) {
        val query = ktVoxApi.createAudioQuery(
            text = inputTextFile.readText(),
            speaker = 1,
        ).body() ?: return
        val soundData = ktVoxApi.postSynthesis(
            speaker = 1,
            audioQuery = query,
        ).body() ?: return
        outputSoundFile.writeBytes(soundData.bytes())
    }
}
