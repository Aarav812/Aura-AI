package com.aura.ai

import app.cash.turbine.test
import com.aura.ai.data.remote.streaming.StreamingParser
import com.aura.ai.domain.model.ChatStreamEvent
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamingParserTest {

    private val parser = StreamingParser()

    private fun sse(vararg lines: String) =
        lines.joinToString("\n").toResponseBody("text/event-stream".toMediaType())

    @Test
    fun `parses tokens and completes`() = runTest {
        val body = sse(
            """data: {"choices":[{"delta":{"content":"Hel"}}]}""",
            """data: {"choices":[{"delta":{"content":"lo"}}]}""",
            "data: [DONE]"
        )
        val events = mutableListOf<ChatStreamEvent>()
        parser.parse(body).test {
            repeat(3) { events.add(awaitItem()) }
            awaitComplete()
        }
        assertEquals(ChatStreamEvent.Token("Hel"), events[0])
        assertEquals(ChatStreamEvent.Token("lo"), events[1])
        val completed = events[2] as ChatStreamEvent.Completed
        assertEquals("Hello", completed.fullText)
    }

    @Test
    fun `ignores malformed lines gracefully`() = runTest {
        val body = sse(
            "garbage line",
            """data: {"choices":[{"delta":{"content":"Hi"}}]}""",
            "data: not-json",
            "data: [DONE]"
        )
        parser.parse(body).test {
            assertEquals(ChatStreamEvent.Token("Hi"), awaitItem())
            val completed = awaitItem() as ChatStreamEvent.Completed
            assertTrue(completed.fullText == "Hi")
            awaitComplete()
        }
    }

    @Test
    fun `reports a stream that disconnects before completion`() = runTest {
        val body = sse(
            """data: {"choices":[{"delta":{"content":"Partial"}}]}"""
        )
        parser.parse(body).test {
            assertEquals(ChatStreamEvent.Token("Partial"), awaitItem())
            assertEquals(
                ChatStreamEvent.Failed("The response stream ended unexpectedly."),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun `emits server errors instead of a successful empty completion`() = runTest {
        val body = sse(
            """data: {"error":{"message":"Model is unavailable"}}"""
        )
        parser.parse(body).test {
            assertEquals(ChatStreamEvent.Failed("Model is unavailable"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `captures reasoning deltas`() = runTest {
        val body = sse(
            """data: {"choices":[{"delta":{"reasoning_content":"thinking"}}]}""",
            """data: {"choices":[{"delta":{"content":"answer"}}]}""",
            "data: [DONE]"
        )
        parser.parse(body).test {
            assertEquals(ChatStreamEvent.Reasoning("thinking"), awaitItem())
            assertEquals(ChatStreamEvent.Token("answer"), awaitItem())
            val completed = awaitItem() as ChatStreamEvent.Completed
            assertEquals("thinking", completed.reasoning)
            awaitComplete()
        }
    }
}
