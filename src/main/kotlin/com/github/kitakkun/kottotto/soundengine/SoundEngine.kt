package com.github.kitakkun.kottotto.soundengine

import java.io.File

interface SoundEngine {
    fun generateSoundFileFromText(inputTextFilePath: String): File?
}