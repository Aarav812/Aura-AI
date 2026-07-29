package com.aura.ai

import com.aura.ai.data.remote.PromptManager
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.ResponseStyle
import com.aura.ai.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptManagerTest {
    private val pm = PromptManager()

    private fun msg(role: Role, text: String) =
        Message(id = text, chatId = "c", role = role, text = text)

    @Test fun `prepends system prompt`() {
        val out = pm.buildMessages(
            AiModel.default,
            listOf(msg(Role.USER, "Hello")),
            AppPreferences()
        )
        assertEquals("system", out.first().role)
        assertEquals("user", out.last().role)
    }

    @Test fun `concise style adjusts system prompt`() {
        val prompt = pm.buildSystemPrompt(AppPreferences(responseStyle = ResponseStyle.CONCISE))
        assertTrue(prompt.contains("concise", ignoreCase = true))
    }

    @Test fun `memory disabled keeps only last turn`() {
        val history = (1..5).map { msg(Role.USER, "m$it") }
        val out = pm.buildMessages(AiModel.default, history, AppPreferences(memoryEnabled = false))
        // system + 1 user message
        assertEquals(2, out.size)
        assertEquals("m5", out.last().content)
    }
}
