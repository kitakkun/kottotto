package com.github.kitakkun.kottotto

import com.github.kitakkun.kottotto.soundengine.VoiceVoxEngine
import org.junit.jupiter.api.Test
import java.io.File

class TestVoiceVoxEngine {
    @Test
    fun queryGenerationTest() {
        val engine = VoiceVoxEngine()
        val inputTextFile = File("input.txt")
        inputTextFile.writeText("HOGEHOGE")
        val outputJsonFile = File("output.json")
        engine.generateQueryJsonFileFromText(inputTextFile, outputJsonFile)
        println(outputJsonFile.readText())
    }
}