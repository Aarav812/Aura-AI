package com.aura.ai.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aura.ai.core.ui.components.GlassCard
import com.aura.ai.core.ui.components.clickableNoRipple

/** Chat list item with swipe-to-archive (start) and swipe-to-delete (end). */
@Composable
fun ChatRow(
    title: String,
    preview: String,
    pinned: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.StartToEnd -> { onArchive(); dismissState.reset() }
            else -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val toStart = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (toStart) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 24.dp),
                contentAlignment = if (toStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Icon(
                    if (toStart) Icons.Rounded.Delete else Icons.Rounded.Archive,
                    contentDescription = null,
                    tint = if (toStart) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        GlassCard(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (preview.isNotBlank()) {
                        Text(
                            preview, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    Icons.Rounded.PushPin,
                    contentDescription = if (pinned) "Unpin" else "Pin",
                    tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp).clickableNoRipple(onPinToggle)
                )
            }
        }
    }
}
