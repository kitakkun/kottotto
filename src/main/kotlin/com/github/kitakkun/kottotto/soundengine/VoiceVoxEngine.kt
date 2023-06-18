package com.github.kitakkun.kottotto.soundengine

import com.github.kitakkun.kottotto.Config
import java.io.File

class VoiceVoxEngine : SoundEngine {
    override fun generateSoundFileFromText(
        inputTextFile: File,
        outputSoundFile: File,
    ) {
        val queryFile = File(inputTextFile.parentFile, "query_${inputTextFile.nameWithoutExtension}.json")
        generateQueryJsonFileFromText(inputTextFile, queryFile)
        generateSoundFileFromQueryJsonFile(queryFile, outputSoundFile)
    }

    fun generateSoundFileFromQueryJsonFile(
        inputQueryJsonFile: File,
        outputSoundFile: File,
    ) {
        val serverUrl = Config.get("VOICEVOX_SERVER_URL") ?: return

        val command = listOf(
            "curl",
            "-s",
            "-H", "Content-Type: application/json",
            "-X", "POST",
            "-d", "@${inputQueryJsonFile.absolutePath}",
            "$serverUrl/synthesis?speaker=1"
        )

        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.start()

        val output = process.inputStream.readBytes()
        outputSoundFile.writeBytes(output)

        val errors = process.errorStream.bufferedReader().use { it.readText() }
        if (errors.isNotEmpty()) {
            println("Errors occurred during the execution:")
            println(errors)
        }
    }

    fun generateQueryJsonFileFromText(
        inputTextFile: File,
        outputQueryJsonFile: File,
    ) {
        val serverUrl = Config.get("VOICEVOX_SERVER_URL") ?: return

        val command = listOf(
            "curl",
            "-s",
            "-X", "POST",
            "$serverUrl/audio_query?speaker=1",
            "--get", "--data-urlencode", "text@${inputTextFile.absolutePath}",
        )

        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        outputQueryJsonFile.writeText(output)

        val errors = process.errorStream.bufferedReader().use { it.readText() }
        if (errors.isNotEmpty()) {
            println("Errors occurred during the execution:")
            println(errors)
        }
    }
}