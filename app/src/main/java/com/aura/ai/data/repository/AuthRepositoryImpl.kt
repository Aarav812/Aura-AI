package com.aura.ai.data.repository

import com.aura.ai.BuildConfig
import com.aura.ai.core.common.AppError
import com.aura.ai.core.common.Resource
import com.aura.ai.domain.model.Plan
import com.aura.ai.domain.model.UserProfile
import com.aura.ai.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val localUser = MutableStateFlow<UserProfile?>(null)

    override val currentUser: Flow<UserProfile?> = if (BuildConfig.FIREBASE_CONFIGURED) {
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { fb ->
                trySend(fb.currentUser?.toProfile())
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }
    } else {
        localUser
    }

    override fun isSignedIn(): Boolean = if (BuildConfig.FIREBASE_CONFIGURED) {
        auth.currentUser != null
    } else {
        localUser.value != null
    }

    override suspend fun signInWithEmail(email: String, password: String): Resource<UserProfile> =
        ifFirebaseConfigured { authCall { auth.signInWithEmailAndPassword(email, password).await().user } }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserProfile> =
        ifFirebaseConfigured {
            authCall {
                val user = auth.createUserWithEmailAndPassword(email, password).await().user
                user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
                user?.let { syncProfile(it) }
                user
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Resource<UserProfile> = ifFirebaseConfigured {
        authCall {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val user = auth.signInWithCredential(credential).await().user
            user?.let { syncProfile(it) }
            user
        }
    }

    override suspend fun signInAnonymously(): Resource<UserProfile> {
        if (BuildConfig.FIREBASE_CONFIGURED) {
            return authCall { auth.signInAnonymously().await().user }
        }
        return UserProfile(
            uid = "local-guest",
            name = "Guest",
            email = "",
            isAnonymous = true
        ).let { guest ->
            localUser.value = guest
            Resource.Success(guest)
        }
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> =
        ifFirebaseConfigured {
            try {
                auth.sendPasswordResetEmail(email).await()
                Resource.Success(Unit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Resource.Error(AppError.Auth(e.message ?: "Could not send reset email"))
            }
        }

    override suspend fun updateProfile(name: String?, photoUrl: String?): Resource<UserProfile> {
        if (!BuildConfig.FIREBASE_CONFIGURED) {
            val updated = localUser.value?.copy(
                name = name ?: localUser.value?.name.orEmpty(),
                photoUrl = photoUrl ?: localUser.value?.photoUrl
            ) ?: return Resource.Error(AppError.Auth("No signed-in account"))
            localUser.value = updated
            return Resource.Success(updated)
        }
        return authCall {
            val user = auth.currentUser
            val req: UserProfileChangeRequest = userProfileChangeRequest {
                name?.let { displayName = it }
                photoUrl?.let { photoUri = android.net.Uri.parse(it) }
            }
            user?.updateProfile(req)?.await()
            user?.let { syncProfile(it) }
            user
        }
    }

    override suspend fun signOut() {
        if (BuildConfig.FIREBASE_CONFIGURED) auth.signOut() else localUser.value = null
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        if (!BuildConfig.FIREBASE_CONFIGURED) {
            localUser.value = null
            return Resource.Success(Unit)
        }
        return try {
            val user = auth.currentUser ?: error("No signed-in account")
            // Firestore rules require an authenticated user, so remove cloud data first.
            firestore.collection("users").document(user.uid).delete().await()
            user.delete().await()
            Resource.Success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Resource.Error(AppError.Auth(e.message ?: "Could not delete account. Re-authenticate and retry."))
        }
    }

    private suspend fun syncProfile(user: FirebaseUser) {
        val data = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "photoUrl" to user.photoUrl?.toString(),
            "updatedAt" to System.currentTimeMillis()
        )
        try {
            firestore.collection("users").document(user.uid)
                .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Authentication succeeded; profile sync can be retried on a later update.
        }
    }

    private suspend fun <T> ifFirebaseConfigured(block: suspend () -> Resource<T>): Resource<T> =
        if (BuildConfig.FIREBASE_CONFIGURED) block()
        else Resource.Error(AppError.Auth("Firebase is not configured. Continue as a guest for local use."))

    private inline fun authCall(block: () -> FirebaseUser?): Resource<UserProfile> = try {
        val user = block()
        if (user != null) Resource.Success(user.toProfile())
        else Resource.Error(AppError.Auth("Authentication failed"))
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (e: Exception) {
        Resource.Error(AppError.Auth(e.message ?: "Authentication failed"))
    }

    private fun FirebaseUser.toProfile() = UserProfile(
        uid = uid,
        name = displayName ?: (if (isAnonymous) "Guest" else email?.substringBefore('@') ?: "User"),
        email = email ?: "",
        photoUrl = photoUrl?.toString(),
        plan = Plan.FREE,
        isAnonymous = isAnonymous
    )
}
