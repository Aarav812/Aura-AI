package com.aura.ai.presentation.chat

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import java.util.Locale

/** Helper to launch the system speech-to-text dialog and parse its result. */
object SpeechRecognizerHelper {
    fun intent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Aura…")
    }

    fun parseResult(result: ActivityResult): String? {
        if (result.resultCode != android.app.Activity.RESULT_OK) return null
        return result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
    }
}
