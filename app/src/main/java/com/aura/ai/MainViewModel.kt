package com.aura.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val preferences: AppPreferences = AppPreferences()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        preferencesRepository.preferences
            .onEach { prefs -> _uiState.value = MainUiState(isLoading = false, preferences = prefs) }
            .launchIn(viewModelScope)
    }
}
