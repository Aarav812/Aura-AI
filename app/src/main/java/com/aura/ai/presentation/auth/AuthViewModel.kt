package com.aura.ai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.core.common.Resource
import com.aura.ai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val authenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onNameChange(v: String) = _state.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun toggleMode() = _state.update {
        it.copy(mode = if (it.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN, error = null)
    }

    fun submitEmail() {
        val s = _state.value
        if (s.email.isBlank() || s.password.length < 6) {
            _state.update { it.copy(error = "Enter a valid email and a 6+ character password") }
            return
        }
        launch {
            val result = if (s.mode == AuthMode.SIGN_IN)
                authRepository.signInWithEmail(s.email.trim(), s.password)
            else authRepository.signUpWithEmail(s.name.trim().ifBlank { "User" }, s.email.trim(), s.password)
            handle(result)
        }
    }

    fun signInWithGoogle(idToken: String) = launch {
        handle(authRepository.signInWithGoogle(idToken))
    }

    fun signInAnonymously() = launch {
        handle(authRepository.signInAnonymously())
    }

    fun forgotPassword() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(error = "Enter your email first") }
            return
        }
        launch {
            when (val r = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> _state.update { it.copy(info = "Password reset email sent") }
                is Resource.Error -> _state.update { it.copy(error = r.error.message) }
                else -> Unit
            }
        }
    }

    private fun <T> handle(result: Resource<T>) {
        when (result) {
            is Resource.Success -> _state.update { it.copy(isLoading = false, authenticated = true) }
            is Resource.Error -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            Resource.Loading -> _state.update { it.copy(isLoading = true) }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        _state.update { it.copy(isLoading = true, error = null, info = null) }
        viewModelScope.launch { block() }
    }
}
