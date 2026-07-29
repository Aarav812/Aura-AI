package com.aura.ai.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class ResponseStyle { BALANCED, CONCISE, DETAILED, CREATIVE, PROFESSIONAL }

/** All user-configurable AI + app preferences, persisted via DataStore. */
data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val language: String = "en",
    val defaultModel: String = AiModel.default.id,
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val streaming: Boolean = true,
    val memoryEnabled: Boolean = true,
    val reasoningEnabled: Boolean = false,
    val internetEnabled: Boolean = false,
    val voiceEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val responseStyle: ResponseStyle = ResponseStyle.BALANCED,
    val systemPrompt: String = "",
    val onboardingComplete: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val highContrast: Boolean = false,
    val fontScale: Float = 1.0f
)
