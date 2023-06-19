package com.github.kitakkun.kottotto.soundengine

import com.github.kitakkun.kottotto.Config
import java.io.File

class OpenJTalkEngine : SoundEngine {
    override suspend fun generateSoundFileFromText(
        inputTextFile: File,
        outputSoundFile: File,
    ) {
        val openJTalkExecutable = Config.get("OPEN_JTALK") ?: return
        val voice = Config.get("OPEN_JTALK_HTS_VOICE") ?: return
        val dictionary = Config.get("OPEN_JTALK_DICTIONARY") ?: return
        ProcessBuilder(
            openJTalkExecutable,
            "-m",
            voice,
            "-x",
            dictionary,
            "-ow",
            outputSoundFile.absolutePath,
            inputTextFile.absolutePath,
        ).start().apply {
            waitFor()
            destroy()
        }
    }
}
