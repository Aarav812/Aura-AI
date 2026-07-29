package com.aura.ai

import com.aura.ai.utils.RateLimiter
import com.aura.ai.utils.TokenCounter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenCounterTest {
    @Test fun `empty text is zero tokens`() = assertEquals(0, TokenCounter.estimate(""))
    @Test fun `estimates scale with length`() {
        val short = TokenCounter.estimate("hi")
        val long = TokenCounter.estimate("this is a considerably longer sentence with many words")
        assertTrue(long > short)
    }
}

class RateLimiterTest {
    @Test fun `allows within window then blocks`() = runTest {
        val limiter = RateLimiter()
        val now = 1_000_000L
        repeat(20) { assertTrue(limiter.tryAcquire(now)) }
        assertFalse(limiter.tryAcquire(now))
    }

    @Test fun `resets at exact window boundary`() = runTest {
        val limiter = RateLimiter()
        val start = 1_000_000L
        repeat(20) { limiter.tryAcquire(start) }
        assertFalse(limiter.tryAcquire(start))
        assertTrue(limiter.tryAcquire(start + 60_000L))
    }

    @Test fun `retry delay rounds up to the next second`() = runTest {
        val limiter = RateLimiter()
        val start = 1_000_000L
        repeat(20) { limiter.tryAcquire(start) }
        assertEquals(60, limiter.retryAfterSeconds(start + 1))
    }
}
