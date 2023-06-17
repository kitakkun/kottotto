package com.github.kitakkun.kottotto.soundengine

import com.github.kitakkun.kottotto.Config
import runCommand
import java.io.File

// TODO: support voicevox
class VoiceVoxEngine : SoundEngine {
    override fun generateSoundFileFromText(inputTextFilePath: String): File? {
        val serverUrl = Config.get("VOICEVOX_SERVER_URL") ?: return null
        val queryProcess = """
                curl -s \
                -X POST" \
                "$serverUrl/audio_query?speaker=1" \
                --get -data-urlencode text@$inputTextFilePath \
                > query.json
                """
            .runCommand()
        queryProcess.waitFor()
        queryProcess.destroy()
        val synthesisProcess = """
            curl -s \
            -H "Content-Type: application/json" \
            -X POST \
            -d @query.json \
            "$serverUrl/synthesis?speaker=1" \
            > audio.wav
        """.runCommand()
        synthesisProcess.waitFor()
        synthesisProcess.destroy()
        return File("audio.wav")
    }
}