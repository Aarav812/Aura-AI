package com.aura.ai.di

import android.content.Context
import com.aura.ai.data.repository.AuthRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    /**
     * The app supports a local guest mode without google-services.json. Firebase SDK classes still
     * require a default app to be present, so use inert options in that configuration. No request is
     * made through this fallback; [AuthRepositoryImpl] handles the local session itself.
     */
    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp =
        FirebaseApp.getApps(context).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:0:android:local")
                    .setApiKey("local-development-key")
                    .setProjectId("aura-local")
                    .build()
            )

    @Provides
    @Singleton
    fun provideAuth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)

    @Provides
    @Singleton
    fun provideFirestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app)

    @Provides
    @Singleton
    fun provideAnalytics(@ApplicationContext context: Context, app: FirebaseApp): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)
}
