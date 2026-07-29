package com.aura.ai.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/** Injectable dispatchers so coroutines are testable (swap for test dispatchers in tests). */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main get() = Dispatchers.Main
    override val io get() = Dispatchers.IO
    override val default get() = Dispatchers.Default
}
