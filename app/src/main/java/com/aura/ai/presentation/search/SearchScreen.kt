package com.aura.ai.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.EmptyState
import com.aura.ai.core.ui.components.GlassCard

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            OutlinedTextField(
                value = query, onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search chats, messages…") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isBlank()) {
            EmptyState(Icons.Rounded.Search, "Search everything",
                "Find chats, messages, files, bookmarks and prompts.", Modifier.fillMaxSize())
        } else if (results.chats.isEmpty() && results.messages.isEmpty()) {
            EmptyState(Icons.Rounded.Search, "No results", "Try different keywords.", Modifier.fillMaxSize())
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (results.chats.isNotEmpty()) {
                    item { Text("Chats", style = MaterialTheme.typography.titleMedium) }
                    items(results.chats, key = { "c_" + it.id }) { chat ->
                        GlassCard(onClick = { onOpenChat(chat.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(chat.title, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                if (results.messages.isNotEmpty()) {
                    item { Text("Messages", style = MaterialTheme.typography.titleMedium) }
                    items(results.messages, key = { "m_" + it.id }) { msg ->
                        GlassCard(onClick = { onOpenChat(msg.chatId) }, modifier = Modifier.fillMaxWidth()) {
                            Text(msg.text, Modifier.padding(16.dp), maxLines = 2, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
