package com.aura.ai.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val plan: Plan = Plan.FREE,
    val isAnonymous: Boolean = false
)

enum class Plan(val displayName: String) { FREE("Free"), PLUS("Aura Plus"), PRO("Aura Pro") }
