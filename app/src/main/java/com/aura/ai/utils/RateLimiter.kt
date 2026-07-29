package com.aura.ai.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple sliding-window client-side rate limiter to protect the NVIDIA quota
 * and give fast feedback before hitting a 429.
 */
@Singleton
class RateLimiter @Inject constructor() {
    private val mutex = Mutex()
    private val timestamps = ArrayDeque<Long>()
    private val windowMs = 60_000L
    private val maxRequestsPerWindow = 20

    /** @return true if allowed, false if the caller should back off. */
    suspend fun tryAcquire(now: Long = System.currentTimeMillis()): Boolean = mutex.withLock {
        while (timestamps.isNotEmpty() && now - timestamps.first() > windowMs) {
            timestamps.removeFirst()
        }
        if (timestamps.size >= maxRequestsPerWindow) return@withLock false
        timestamps.addLast(now)
        true
    }

    suspend fun retryAfterSeconds(now: Long = System.currentTimeMillis()): Long = mutex.withLock {
        val oldest = timestamps.firstOrNull() ?: return@withLock 0
        ((windowMs - (now - oldest)) / 1000).coerceAtLeast(1)
    }
}
