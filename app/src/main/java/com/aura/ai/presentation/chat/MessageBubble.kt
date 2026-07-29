package com.aura.ai.presentation.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aura.ai.core.ui.components.MarkdownText
import com.aura.ai.core.ui.components.clickableNoRipple
import com.aura.ai.domain.model.Feedback
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.MessageStatus
import com.aura.ai.domain.model.Role

@Composable
fun MessageBubble(
    message: Message,
    isLast: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onSpeak: () -> Unit,
    onFeedback: (Feedback) -> Unit
) {
    val isUser = message.role == Role.USER
    Column(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
                .padding(14.dp)
        ) {
            when {
                message.status == MessageStatus.STREAMING && message.text.isBlank() -> TypingIndicator()
                isUser -> Text(message.text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                else -> Column {
                    message.reasoning?.takeIf { it.isNotBlank() }?.let {
                        Text("Reasoning", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Text(it, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp))
                    }
                    MarkdownText(message.text)
                    if (message.status == MessageStatus.STREAMING) StreamingCursor()
                    if (message.status == MessageStatus.ERROR) {
                        Text("⚠︎ Failed to generate", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Action row for completed assistant messages
        if (!isUser && message.status == MessageStatus.COMPLETE) {
            Row(
                Modifier.padding(top = 4.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIcon(Icons.Rounded.ContentCopy, "Copy", onCopy)
                ActionIcon(Icons.Rounded.VolumeUp, "Read aloud", onSpeak)
                if (isLast) ActionIcon(Icons.Rounded.Refresh, "Regenerate", onRegenerate)
                ActionIcon(
                    Icons.Rounded.ThumbUp, "Like",
                    { onFeedback(if (message.feedback == Feedback.LIKED) Feedback.NONE else Feedback.LIKED) },
                    active = message.feedback == Feedback.LIKED
                )
                ActionIcon(
                    Icons.Rounded.ThumbDown, "Dislike",
                    { onFeedback(if (message.feedback == Feedback.DISLIKED) Feedback.NONE else Feedback.DISLIKED) },
                    active = message.feedback == Feedback.DISLIKED
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
    active: Boolean = false
) {
    Icon(
        icon, desc,
        modifier = Modifier.size(18.dp).clickableNoRipple(onClick),
        tint = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}

@Composable
private fun StreamingCursor() {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "cursorAlpha"
    )
    Box(Modifier.size(width = 8.dp, height = 16.dp).alpha(alpha)
        .background(MaterialTheme.colorScheme.primary))
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val scale by transition.animateFloat(
                initialValue = 0.4f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(600, delayMillis = i * 150), RepeatMode.Reverse),
                label = "dot$i"
            )
            Box(
                Modifier.size(8.dp).alpha(scale)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
        }
    }
}
