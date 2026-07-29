package com.aura.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.repository.AuthRepository
import com.aura.ai.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val isSignedIn: Boolean = false,
    val preferences: AppPreferences = AppPreferences()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            authRepository.currentUser,
            preferencesRepository.preferences
        ) { user, prefs ->
            MainUiState(isLoading = false, isSignedIn = user != null, preferences = prefs)
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }
}
