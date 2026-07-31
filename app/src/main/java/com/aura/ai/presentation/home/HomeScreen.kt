package com.aura.ai.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aura.ai.core.ui.components.clickableNoRipple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.AuraChip
import com.aura.ai.core.ui.components.EmptyState
import com.aura.ai.core.ui.components.OfflineBanner
import com.aura.ai.core.ui.theme.GradientEnd
import com.aura.ai.core.ui.theme.GradientStart

private data class QuickAction(val label: String, val emoji: String, val prompt: String)

private val quickActions = listOf(
    QuickAction("Create Image", "🎨", "Create an image of "),
    QuickAction("Research", "🔎", "Research the topic: "),
    QuickAction("Code", "💻", "Help me write code that "),
    QuickAction("Travel", "✈️", "Plan a trip to "),
    QuickAction("Study", "📚", "Explain this concept simply: "),
    QuickAction("Summarize", "📝", "Summarize the following: "),
    QuickAction("Writing", "✍️", "Help me write "),
    QuickAction("Business", "💼", "Give me a business plan for ")
)

@Composable
fun HomeScreen(
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewChat,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.White)
                Spacer(Modifier.size(8.dp))
                Text("New Chat", color = Color.White)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(!state.isOnline) { OfflineBanner() }

            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { TopBar(onOpenSearch) }
                item {
                    Column {
                        Text(state.greeting + ",", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "there",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                item {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        quickActions.forEach { qa ->
                            AuraChip(label = qa.label, emoji = qa.emoji) { onNewChat() }
                        }
                    }
                }

                if (state.pinnedChats.isNotEmpty()) {
                    item { SectionHeader("Pinned", Icons.Rounded.PushPin) }
                    items(state.pinnedChats, key = { it.id }) { chat ->
                        ChatRow(
                            title = chat.title, preview = chat.preview, pinned = true,
                            onClick = { onOpenChat(chat.id) },
                            onPinToggle = { viewModel.pin(chat.id, false) },
                            onArchive = { viewModel.archive(chat.id) },
                            onDelete = { viewModel.delete(chat.id) }
                        )
                    }
                }

                item { SectionHeader("Recent") }
                if (state.recentChats.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.AutoAwesome,
                            title = "No conversations yet",
                            subtitle = "Tap New Chat to start talking with Aura."
                        )
                    }
                } else {
                    items(state.recentChats, key = { it.id }) { chat ->
                        ChatRow(
                            title = chat.title, preview = chat.preview, pinned = false,
                            onClick = { onOpenChat(chat.id) },
                            onPinToggle = { viewModel.pin(chat.id, true) },
                            onArchive = { viewModel.archive(chat.id) },
                            onDelete = { viewModel.delete(chat.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onSearch: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.size(10.dp))
        Text("Aura AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Rounded.Search, "Search", modifier = Modifier.size(26.dp).clip(CircleShape).clickableNoRipple(onSearch))
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        icon?.let { Icon(it, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}


