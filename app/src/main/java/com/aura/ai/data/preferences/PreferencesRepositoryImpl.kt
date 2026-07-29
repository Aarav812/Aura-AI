package com.aura.ai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.ResponseStyle
import com.aura.ai.domain.model.ThemeMode
import com.aura.ai.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_prefs")

private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
    value?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } } ?: default

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val LANGUAGE = stringPreferencesKey("language")
        val MODEL = stringPreferencesKey("default_model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_P = floatPreferencesKey("top_p")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val STREAMING = booleanPreferencesKey("streaming")
        val MEMORY = booleanPreferencesKey("memory")
        val REASONING = booleanPreferencesKey("reasoning")
        val INTERNET = booleanPreferencesKey("internet")
        val VOICE = booleanPreferencesKey("voice")
        val TTS = booleanPreferencesKey("tts")
        val STYLE = stringPreferencesKey("response_style")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    override val preferences: Flow<AppPreferences> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { p ->
            val defaults = AppPreferences()
            AppPreferences(
                themeMode = enumOrDefault(p[Keys.THEME], defaults.themeMode),
                dynamicColor = p[Keys.DYNAMIC] ?: defaults.dynamicColor,
                language = p[Keys.LANGUAGE] ?: defaults.language,
                defaultModel = p[Keys.MODEL] ?: defaults.defaultModel,
                temperature = p[Keys.TEMPERATURE] ?: defaults.temperature,
                topP = p[Keys.TOP_P] ?: defaults.topP,
                maxTokens = p[Keys.MAX_TOKENS] ?: defaults.maxTokens,
                streaming = p[Keys.STREAMING] ?: defaults.streaming,
                memoryEnabled = p[Keys.MEMORY] ?: defaults.memoryEnabled,
                reasoningEnabled = p[Keys.REASONING] ?: defaults.reasoningEnabled,
                internetEnabled = p[Keys.INTERNET] ?: defaults.internetEnabled,
                voiceEnabled = p[Keys.VOICE] ?: defaults.voiceEnabled,
                ttsEnabled = p[Keys.TTS] ?: defaults.ttsEnabled,
                responseStyle = enumOrDefault(p[Keys.STYLE], defaults.responseStyle),
                systemPrompt = p[Keys.SYSTEM_PROMPT] ?: defaults.systemPrompt,
                onboardingComplete = p[Keys.ONBOARDING] ?: defaults.onboardingComplete,
                dailyReminderEnabled = p[Keys.DAILY_REMINDER] ?: defaults.dailyReminderEnabled,
                highContrast = p[Keys.HIGH_CONTRAST] ?: defaults.highContrast,
                fontScale = p[Keys.FONT_SCALE] ?: defaults.fontScale
            )
        }

    override suspend fun update(transform: (AppPreferences) -> AppPreferences) {
        context.dataStore.edit { p ->
            val current = AppPreferences(
                themeMode = enumOrDefault(p[Keys.THEME], ThemeMode.SYSTEM),
                dynamicColor = p[Keys.DYNAMIC] ?: true,
                language = p[Keys.LANGUAGE] ?: "en",
                defaultModel = p[Keys.MODEL] ?: AppPreferences().defaultModel,
                temperature = p[Keys.TEMPERATURE] ?: 0.7f,
                topP = p[Keys.TOP_P] ?: 0.95f,
                maxTokens = p[Keys.MAX_TOKENS] ?: 2048,
                streaming = p[Keys.STREAMING] ?: true,
                memoryEnabled = p[Keys.MEMORY] ?: true,
                reasoningEnabled = p[Keys.REASONING] ?: false,
                internetEnabled = p[Keys.INTERNET] ?: false,
                voiceEnabled = p[Keys.VOICE] ?: true,
                ttsEnabled = p[Keys.TTS] ?: true,
                responseStyle = enumOrDefault(p[Keys.STYLE], ResponseStyle.BALANCED),
                systemPrompt = p[Keys.SYSTEM_PROMPT] ?: "",
                onboardingComplete = p[Keys.ONBOARDING] ?: false,
                dailyReminderEnabled = p[Keys.DAILY_REMINDER] ?: false,
                highContrast = p[Keys.HIGH_CONTRAST] ?: false,
                fontScale = p[Keys.FONT_SCALE] ?: 1.0f
            )
            val next = transform(current)
            p[Keys.THEME] = next.themeMode.name
            p[Keys.DYNAMIC] = next.dynamicColor
            p[Keys.LANGUAGE] = next.language
            p[Keys.MODEL] = next.defaultModel
            p[Keys.TEMPERATURE] = next.temperature
            p[Keys.TOP_P] = next.topP
            p[Keys.MAX_TOKENS] = next.maxTokens
            p[Keys.STREAMING] = next.streaming
            p[Keys.MEMORY] = next.memoryEnabled
            p[Keys.REASONING] = next.reasoningEnabled
            p[Keys.INTERNET] = next.internetEnabled
            p[Keys.VOICE] = next.voiceEnabled
            p[Keys.TTS] = next.ttsEnabled
            p[Keys.STYLE] = next.responseStyle.name
            p[Keys.SYSTEM_PROMPT] = next.systemPrompt
            p[Keys.ONBOARDING] = next.onboardingComplete
            p[Keys.DAILY_REMINDER] = next.dailyReminderEnabled
            p[Keys.HIGH_CONTRAST] = next.highContrast
            p[Keys.FONT_SCALE] = next.fontScale
        }
    }
}
