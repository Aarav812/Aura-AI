package com.aura.ai.data.remote

import com.aura.ai.data.remote.dto.ChatMessageDto
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.ResponseStyle
import com.aura.ai.domain.model.Role
import com.aura.ai.utils.TokenCounter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the message payload sent to the model:
 *  - composes the system prompt (base persona + style + user overrides)
 *  - applies conversation memory windowing to respect the context window
 */
@Singleton
class PromptManager @Inject constructor() {

    fun buildMessages(
        model: AiModel,
        history: List<Message>,
        prefs: AppPreferences
    ): List<ChatMessageDto> {
        val system = buildSystemPrompt(prefs)
        val budget = (model.contextWindow - prefs.maxTokens - TokenCounter.estimate(system))
            .coerceAtLeast(1000)

        val windowed = if (prefs.memoryEnabled) {
            trimToBudget(history, budget)
        } else {
            history.takeLast(1) // no memory: only the latest user turn
        }

        return buildList {
            add(ChatMessageDto(role = "system", content = system))
            windowed.filter { it.role != Role.SYSTEM }.forEach {
                add(ChatMessageDto(role = it.role.name.lowercase(), content = it.text))
            }
        }
    }

    private fun trimToBudget(history: List<Message>, budget: Int): List<Message> {
        var used = 0
        val result = ArrayDeque<Message>()
        for (msg in history.asReversed()) {
            val cost = if (msg.tokenCount > 0) msg.tokenCount else TokenCounter.estimate(msg.text)
            if (used + cost > budget && result.isNotEmpty()) break
            used += cost
            result.addFirst(msg)
        }
        return result.toList()
    }

    fun buildSystemPrompt(prefs: AppPreferences): String = buildString {
        append("You are Aura, a helpful, friendly and highly capable AI assistant. ")
        append("Format responses in clean Markdown. Use fenced code blocks with a language tag, ")
        append("tables, and LaTeX ($...$ / $$...$$) where helpful. ")
        when (prefs.responseStyle) {
            ResponseStyle.CONCISE -> append("Be concise and to the point. ")
            ResponseStyle.DETAILED -> append("Be thorough and explain your reasoning. ")
            ResponseStyle.CREATIVE -> append("Be imaginative and expressive. ")
            ResponseStyle.PROFESSIONAL -> append("Maintain a professional, business-appropriate tone. ")
            ResponseStyle.BALANCED -> Unit
        }
        if (prefs.reasoningEnabled) append("Think step by step before answering. ")
        if (prefs.systemPrompt.isNotBlank()) append("\n\nAdditional instructions: ${prefs.systemPrompt}")
    }

    fun titlePrompt(firstUserMessage: String): List<ChatMessageDto> = listOf(
        ChatMessageDto(
            role = "system",
            content = "Generate a short, specific chat title (3-6 words, no quotes, no trailing punctuation) " +
                "summarizing the user's request."
        ),
        ChatMessageDto(role = "user", content = firstUserMessage.take(500))
    )
}
