package com.aura.ai.utils

/**
 * Lightweight heuristic token estimator (~4 chars/token, English-biased).
 * Good enough for UI counters & context-window trimming without a native tokenizer.
 */
object TokenCounter {
    private const val CHARS_PER_TOKEN = 4.0

    fun estimate(text: String): Int {
        if (text.isBlank()) return 0
        val byChars = Math.ceil(text.length / CHARS_PER_TOKEN).toInt()
        val byWords = Math.ceil(text.trim().split(Regex("\\s+")).size * 1.3).toInt()
        return maxOf(byChars, byWords)
    }

    fun estimateConversation(texts: List<String>): Int = texts.sumOf { estimate(it) }
}
