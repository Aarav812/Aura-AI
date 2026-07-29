package com.aura.ai.presentation.explore

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.ai.core.ui.components.AuraChip
import com.aura.ai.core.ui.components.GlassCard
import com.aura.ai.core.ui.components.clickableNoRipple
import com.aura.ai.domain.model.Prompt
import com.aura.ai.domain.model.PromptCategory

@Composable
fun ExploreScreen(onRunPrompt: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf<PromptCategory?>(null) }
    val prompts = remember(selectedCategory) {
        selectedCategory?.let { PromptCatalog.byCategory(it) } ?: PromptCatalog.all
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Explore", style = MaterialTheme.typography.displayLarge)
            Text("Discover what Aura can do", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuraChip("All", selected = selectedCategory == null) { selectedCategory = null }
                PromptCategory.entries.forEach { cat ->
                    AuraChip(cat.displayName, cat.emoji, selected = selectedCategory == cat) {
                        selectedCategory = cat
                    }
                }
            }
        }
        if (selectedCategory == null) {
            item { Text("🔥 Trending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(PromptCatalog.trending, key = { "t_" + it.id }) { PromptCard(it, onRunPrompt) }
            item { Text("⭐ Featured Workflows", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(PromptCatalog.featured, key = { "f_" + it.id }) { PromptCard(it, onRunPrompt) }
        } else {
            items(prompts, key = { it.id }) { PromptCard(it, onRunPrompt) }
        }
    }
}

@Composable
private fun PromptCard(prompt: Prompt, onRun: (String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${prompt.category.emoji}  ${prompt.title}", style = MaterialTheme.typography.titleMedium)
                Text(prompt.body, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2)
            }
            Icon(
                Icons.Rounded.PlayArrow, "Run prompt",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickableNoRipple { onRun(prompt.body) }
            )
        }
    }
}
