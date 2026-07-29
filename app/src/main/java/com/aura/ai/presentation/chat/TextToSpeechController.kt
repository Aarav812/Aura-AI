package com.aura.ai.presentation.chat

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Thin wrapper over Android TextToSpeech for message playback. */
class TextToSpeechController(context: Context) {
    private var ready = false
    private val tts = TextToSpeech(context) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }

    fun speak(text: String) {
        if (ready && text.isNotBlank()) {
            tts.speak(text.take(4000), TextToSpeech.QUEUE_FLUSH, null, "aura_utterance")
        }
    }

    fun stop() { if (ready) tts.stop() }
    fun shutdown() { tts.stop(); tts.shutdown() }
}
