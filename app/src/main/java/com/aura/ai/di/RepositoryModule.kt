package com.aura.ai.di

import com.aura.ai.core.common.DefaultDispatcherProvider
import com.aura.ai.core.common.DispatcherProvider
import com.aura.ai.data.preferences.PreferencesRepositoryImpl
import com.aura.ai.data.repository.AiRepositoryImpl
import com.aura.ai.data.repository.AuthRepositoryImpl
import com.aura.ai.data.repository.ChatRepositoryImpl
import com.aura.ai.domain.repository.AiRepository
import com.aura.ai.domain.repository.AuthRepository
import com.aura.ai.domain.repository.ChatRepository
import com.aura.ai.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
