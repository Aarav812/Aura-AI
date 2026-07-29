package com.aura.ai.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.aura.ai.domain.model.AiModel

@Composable
fun ModelSelectorButton(selected: AiModel, onSelect: (AiModel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    TextButton(onClick = { expanded = true }) {
        Text(selected.displayName, style = MaterialTheme.typography.labelLarge)
        Icon(Icons.Rounded.ExpandMore, "Change model")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        AiModel.entries.forEach { model ->
            DropdownMenuItem(
                text = {
                    Column(modifier = androidx.compose.ui.Modifier.padding(vertical = 2.dp)) {
                        Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(model.description, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                onClick = { onSelect(model); expanded = false }
            )
        }
    }
}
