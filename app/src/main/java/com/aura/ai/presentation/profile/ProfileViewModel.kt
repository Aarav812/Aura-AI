package com.aura.ai.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.core.common.Resource
import com.aura.ai.domain.model.UserProfile
import com.aura.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val user = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateName(name: String) = viewModelScope.launch {
        authRepository.updateProfile(name = name, photoUrl = null)
    }

    fun signOut(onDone: () -> Unit) = viewModelScope.launch {
        authRepository.signOut(); onDone()
    }

    fun deleteAccount(onDone: () -> Unit) = viewModelScope.launch {
        if (authRepository.deleteAccount() is Resource.Success) onDone()
    }
}
