package com.github.kitakkun.kottotto.soundengine

import com.github.kitakkun.kottotto.Config
import runCommand
import java.io.File

// TODO: support voicevox
class VoiceVoxEngine : SoundEngine {
    override fun generateSoundFileFromText(
        inputTextFile: File,
        outputSoundFile: File,
    ) {
        val serverUrl = Config.get("VOICEVOX_SERVER_URL") ?: return
        val queryProcess = """
            curl -s \
            -X POST" \
            "$serverUrl/audio_query?speaker=1" \
            --get -data-urlencode text@${inputTextFile.absolutePath} \
            > query.json
            """.runCommand()
        queryProcess.waitFor()
        queryProcess.destroy()
        val synthesisProcess = """
            curl -s \
            -H "Content-Type: application/json" \
            -X POST \
            -d @query.json \
            "$serverUrl/synthesis?speaker=1" \
            > ${outputSoundFile.absolutePath}
        """.runCommand()
        synthesisProcess.waitFor()
        synthesisProcess.destroy()
    }
}