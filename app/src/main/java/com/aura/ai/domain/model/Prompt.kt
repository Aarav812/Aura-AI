package com.aura.ai.domain.model

/** A curated prompt shown in the Explore screen. */
data class Prompt(
    val id: String,
    val title: String,
    val body: String,
    val category: PromptCategory,
    val trending: Boolean = false,
    val featured: Boolean = false,
    val bookmarked: Boolean = false
)

enum class PromptCategory(val displayName: String, val emoji: String) {
    WRITING("Writing", "✍️"),
    CODING("Coding", "💻"),
    DESIGN("Design", "🎨"),
    MARKETING("Marketing", "📣"),
    FINANCE("Finance", "💰"),
    TRAVEL("Travel", "✈️"),
    BUSINESS("Business", "💼"),
    HEALTH("Health", "🩺"),
    EDUCATION("Education", "📚"),
    LEGAL("Legal", "⚖️"),
    IMAGES("Images", "🖼️"),
    VIDEO("Video", "🎬"),
    MUSIC("Music", "🎵")
}
