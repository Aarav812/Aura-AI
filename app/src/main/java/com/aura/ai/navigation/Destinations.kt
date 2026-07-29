package com.aura.ai.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val VOICE = "voice"
    const val CHAT = "chat"                // chat/{chatId}?prompt={prompt}
    const val CHAT_ARG = "chatId"
    const val PROMPT_ARG = "prompt"

    fun chat(chatId: String, prompt: String? = null): String = buildString {
        append("$CHAT/${Uri.encode(chatId)}")
        if (!prompt.isNullOrBlank()) append("?$PROMPT_ARG=${Uri.encode(prompt)}")
    }
}

enum class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Rounded.Home),
    EXPLORE(Routes.EXPLORE, "Explore", Icons.Rounded.Explore),
    LIBRARY(Routes.LIBRARY, "Library", Icons.Rounded.GridView),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Rounded.Settings)
}
