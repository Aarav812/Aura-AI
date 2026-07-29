package com.aura.ai.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.AuraChip
import com.aura.ai.core.ui.components.EmptyState
import com.aura.ai.core.ui.components.GlassCard
import com.aura.ai.core.ui.components.clickableNoRipple

@Composable
fun LibraryScreen(
    onOpenChat: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Library", style = MaterialTheme.typography.displayLarge, modifier = Modifier.weight(1f))
            Icon(
                if (state.layout == LibraryLayout.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                "Toggle layout",
                modifier = Modifier.clickableNoRipple(viewModel::toggleLayout)
            )
        }
        OutlinedTextField(
            value = state.query, onValueChange = viewModel::setQuery,
            placeholder = { Text("Search library") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LibraryFilter.entries.forEach { f ->
                AuraChip(f.label, selected = state.filter == f) { viewModel.setFilter(f) }
            }
        }

        if (state.chats.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.GridView,
                title = "Nothing here yet",
                subtitle = "Your chats, documents and saved items will appear here.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (state.layout == LibraryLayout.GRID) 2 else 1),
                contentPadding = PaddingValues(vertical = 12.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.chats, key = { it.id }) { chat ->
                    GlassCard(onClick = { onOpenChat(chat.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(chat.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                            Text(chat.preview, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}
