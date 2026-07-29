package com.aura.ai.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.core.common.Resource
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.ResponseStyle
import com.aura.ai.domain.model.ThemeMode
import com.aura.ai.domain.model.UserProfile
import com.aura.ai.domain.repository.AuthRepository
import com.aura.ai.domain.repository.PreferencesRepository
import com.aura.ai.work.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val prefs: AppPreferences = AppPreferences(),
    val user: UserProfile? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val uiState = combine(
        preferencesRepository.preferences,
        authRepository.currentUser
    ) { prefs, user -> SettingsUiState(prefs, user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private fun edit(block: (AppPreferences) -> AppPreferences) = viewModelScope.launch {
        preferencesRepository.update(block)
    }

    fun setTheme(mode: ThemeMode) = edit { it.copy(themeMode = mode) }
    fun setDynamicColor(v: Boolean) = edit { it.copy(dynamicColor = v) }
    fun setModel(id: String) = edit { it.copy(defaultModel = id) }
    fun setTemperature(v: Float) = edit { it.copy(temperature = v) }
    fun setTopP(v: Float) = edit { it.copy(topP = v) }
    fun setMaxTokens(v: Int) = edit { it.copy(maxTokens = v) }
    fun setStreaming(v: Boolean) = edit { it.copy(streaming = v) }
    fun setMemory(v: Boolean) = edit { it.copy(memoryEnabled = v) }
    fun setReasoning(v: Boolean) = edit { it.copy(reasoningEnabled = v) }
    fun setInternet(v: Boolean) = edit { it.copy(internetEnabled = v) }
    fun setVoice(v: Boolean) = edit { it.copy(voiceEnabled = v) }
    fun setTts(v: Boolean) = edit { it.copy(ttsEnabled = v) }
    fun setStyle(v: ResponseStyle) = edit { it.copy(responseStyle = v) }
    fun setSystemPrompt(v: String) = edit { it.copy(systemPrompt = v) }
    fun setLanguage(v: String) = edit { it.copy(language = v) }
    fun setHighContrast(v: Boolean) = edit { it.copy(highContrast = v) }
    fun setDailyReminder(v: Boolean) {
        edit { it.copy(dailyReminderEnabled = v) }
        notificationScheduler.scheduleDailyReminder(v)
    }

    fun signOut(onDone: () -> Unit) = viewModelScope.launch {
        authRepository.signOut(); onDone()
    }

    fun deleteAccount(onDone: () -> Unit) = viewModelScope.launch {
        when (authRepository.deleteAccount()) {
            is Resource.Success -> onDone()
            is Resource.Error -> Unit
            else -> Unit
        }
    }
}
