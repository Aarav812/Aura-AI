package com.aura.ai.domain.repository

import com.aura.ai.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<AppPreferences>
    suspend fun update(transform: (AppPreferences) -> AppPreferences)
}
