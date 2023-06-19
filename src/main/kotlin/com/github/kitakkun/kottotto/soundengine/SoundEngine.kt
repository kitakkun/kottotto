package com.github.kitakkun.kottotto.soundengine

import java.io.File

interface SoundEngine {
    suspend fun generateSoundFileFromText(inputTextFile: File, outputSoundFile: File)
}
