package com.github.kitakkun.kottotto.soundengine

import com.github.kitakkun.kottotto.Config
import java.io.File

class OpenJTalkEngine : SoundEngine {
    override fun generateSoundFileFromText(
        inputTextFilePath: String,
    ): File {
        val openJTalkExecutable = Config.get("OPEN_JTALK")
        val voice = Config.get("OPEN_JTALK_HTS_VOICE")
        val dictionary = Config.get("OPEN_JTALK_DICTIONARY")
        ProcessBuilder(
            "$openJTalkExecutable",
            "-m",
            voice,
            "-x",
            dictionary,
            "-ow",
            "test.wav",
            inputTextFilePath
        ).start().apply {
            waitFor()
            destroy()
        }
        return File("test.wav")
    }
}