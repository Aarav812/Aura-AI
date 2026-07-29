package com.aura.ai.presentation.explore

import com.aura.ai.domain.model.Prompt
import com.aura.ai.domain.model.PromptCategory

/** Curated, bundled prompt catalog powering the Explore screen (works fully offline). */
object PromptCatalog {
    val all: List<Prompt> = listOf(
        Prompt("w1", "Blog post outline", "Create a detailed blog post outline about {topic} with SEO headings.", PromptCategory.WRITING, trending = true, featured = true),
        Prompt("w2", "Rewrite professionally", "Rewrite the following text in a professional tone:\n\n{text}", PromptCategory.WRITING),
        Prompt("w3", "Email polish", "Improve grammar and clarity of this email while keeping my voice:\n\n{email}", PromptCategory.WRITING),
        Prompt("c1", "Explain code", "Explain what this code does step by step:\n\n```\n{code}\n```", PromptCategory.CODING, trending = true),
        Prompt("c2", "Debug helper", "Find and fix the bug in this code and explain the fix:\n\n```\n{code}\n```", PromptCategory.CODING, featured = true),
        Prompt("c3", "Unit tests", "Write comprehensive unit tests for this function:\n\n```\n{code}\n```", PromptCategory.CODING),
        Prompt("d1", "Color palette", "Suggest a modern color palette for a {brand} app with hex codes.", PromptCategory.DESIGN),
        Prompt("d2", "UX critique", "Critique this UX flow and suggest improvements: {flow}", PromptCategory.DESIGN),
        Prompt("m1", "Ad copy", "Write 5 punchy ad headlines for {product} targeting {audience}.", PromptCategory.MARKETING, trending = true),
        Prompt("m2", "Content calendar", "Build a 30-day social content calendar for {brand}.", PromptCategory.MARKETING),
        Prompt("f1", "Budget plan", "Create a monthly budget plan for an income of {amount}.", PromptCategory.FINANCE),
        Prompt("f2", "Investment basics", "Explain index funds vs ETFs for a beginner.", PromptCategory.FINANCE),
        Prompt("t1", "Trip itinerary", "Plan a {days}-day itinerary for {destination} on a {budget} budget.", PromptCategory.TRAVEL, featured = true),
        Prompt("t2", "Packing list", "Create a packing list for {days} days in {destination} in {season}.", PromptCategory.TRAVEL),
        Prompt("b1", "Business plan", "Draft a lean business plan for a {idea} startup.", PromptCategory.BUSINESS, trending = true),
        Prompt("b2", "SWOT analysis", "Do a SWOT analysis for {company}.", PromptCategory.BUSINESS),
        Prompt("h1", "Meal plan", "Create a 7-day healthy meal plan for {goal}.", PromptCategory.HEALTH),
        Prompt("h2", "Workout split", "Design a beginner {days}-day workout split.", PromptCategory.HEALTH),
        Prompt("e1", "Study plan", "Make a study plan to learn {subject} in {weeks} weeks.", PromptCategory.EDUCATION, featured = true),
        Prompt("e2", "Explain simply", "Explain {concept} like I'm 12 years old.", PromptCategory.EDUCATION, trending = true),
        Prompt("l1", "Contract summary", "Summarize the key obligations in this contract:\n\n{text}", PromptCategory.LEGAL),
        Prompt("i1", "Image prompt", "Write a vivid image generation prompt for: {idea}", PromptCategory.IMAGES),
        Prompt("v1", "Video script", "Write a 60-second video script about {topic}.", PromptCategory.VIDEO),
        Prompt("mu1", "Song lyrics", "Write {genre} song lyrics about {theme}.", PromptCategory.MUSIC),
    )

    fun byCategory(category: PromptCategory) = all.filter { it.category == category }
    val trending get() = all.filter { it.trending }
    val featured get() = all.filter { it.featured }
}
