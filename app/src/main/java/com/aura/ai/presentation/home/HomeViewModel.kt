package com.aura.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.model.UserProfile
import com.aura.ai.domain.repository.AuthRepository
import com.aura.ai.domain.repository.ChatRepository
import com.aura.ai.domain.repository.PreferencesRepository
import com.aura.ai.utils.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "Hello",
    val user: UserProfile? = null,
    val pinnedChats: List<Chat> = emptyList(),
    val recentChats: List<Chat> = emptyList(),
    val selectedModel: AiModel = AiModel.default,
    val isOnline: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
    connectivity: ConnectivityObserver
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.currentUser,
        chatRepository.observePinnedChats(),
        chatRepository.observeChats(includeArchived = false),
        preferencesRepository.preferences,
        connectivity.isOnline
    ) { user, pinned, all, prefs, online ->
        HomeUiState(
            greeting = greeting(),
            user = user,
            pinnedChats = pinned,
            recentChats = all.filterNot { it.pinned }.take(50),
            selectedModel = AiModel.fromId(prefs.defaultModel),
            isOnline = online
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun selectModel(model: AiModel) = viewModelScope.launch {
        preferencesRepository.update { it.copy(defaultModel = model.id) }
    }

    fun pin(chatId: String, pinned: Boolean) = viewModelScope.launch {
        chatRepository.setPinned(chatId, pinned)
    }

    fun archive(chatId: String) = viewModelScope.launch { chatRepository.setArchived(chatId, true) }
    fun delete(chatId: String) = viewModelScope.launch { chatRepository.deleteChat(chatId) }

    private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }
}
