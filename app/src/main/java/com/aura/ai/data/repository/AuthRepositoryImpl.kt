package com.aura.ai.data.repository

import com.aura.ai.core.common.AppError
import com.aura.ai.core.common.Resource
import com.aura.ai.domain.model.Plan
import com.aura.ai.domain.model.UserProfile
import com.aura.ai.domain.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fb ->
            trySend(fb.currentUser?.toProfile())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signInWithEmail(email: String, password: String): Resource<UserProfile> =
        authCall { auth.signInWithEmailAndPassword(email, password).await().user }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserProfile> =
        authCall {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
            user?.updateProfile(userProfileChangeRequest { displayName = name })?.await()
            user?.let { syncProfile(it) }
            user
        }

    override suspend fun signInWithGoogle(idToken: String): Resource<UserProfile> = authCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(credential).await().user
        user?.let { syncProfile(it) }
        user
    }

    override suspend fun signInAnonymously(): Resource<UserProfile> =
        authCall { auth.signInAnonymously().await().user }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AppError.Auth(e.message ?: "Could not send reset email"))
    }

    override suspend fun updateProfile(name: String?, photoUrl: String?): Resource<UserProfile> = authCall {
        val user = auth.currentUser
        val req: UserProfileChangeRequest = userProfileChangeRequest {
            name?.let { displayName = it }
            photoUrl?.let { photoUri = android.net.Uri.parse(it) }
        }
        user?.updateProfile(req)?.await()
        user?.let { syncProfile(it) }
        user
    }

    override suspend fun signOut() { auth.signOut() }

    override suspend fun deleteAccount(): Resource<Unit> = try {
        val uid = auth.currentUser?.uid
        auth.currentUser?.delete()?.await()
        uid?.let { firestore.collection("users").document(it).delete() }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(AppError.Auth(e.message ?: "Could not delete account. Re-authenticate and retry."))
    }

    private suspend fun syncProfile(user: FirebaseUser) {
        val data = mapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "photoUrl" to user.photoUrl?.toString(),
            "updatedAt" to System.currentTimeMillis()
        )
        runCatching {
            firestore.collection("users").document(user.uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    private inline fun authCall(block: () -> FirebaseUser?): Resource<UserProfile> = try {
        val user = block()
        if (user != null) Resource.Success(user.toProfile())
        else Resource.Error(AppError.Auth("Authentication failed"))
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
