package com.aura.ai.domain.repository

import com.aura.ai.core.common.Resource
import com.aura.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the current authenticated user, or null when signed out. Restores session on startup. */
    val currentUser: Flow<UserProfile?>

    fun isSignedIn(): Boolean

    suspend fun signInWithEmail(email: String, password: String): Resource<UserProfile>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserProfile>
    suspend fun signInWithGoogle(idToken: String): Resource<UserProfile>
    suspend fun signInAnonymously(): Resource<UserProfile>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun updateProfile(name: String?, photoUrl: String?): Resource<UserProfile>
    suspend fun signOut()
    suspend fun deleteAccount(): Resource<Unit>
}
