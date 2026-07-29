package com.aura.ai.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.ai.core.ui.components.clickableNoRipple
import com.aura.ai.core.ui.theme.GradientEnd
import com.aura.ai.core.ui.theme.GradientStart

/** Large rounded auto-expanding composer with attachment, mic, voice & send actions. */
@Composable
fun AiComposer(
    value: String,
    isGenerating: Boolean,
    voiceEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMic: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onFiles: () -> Unit,
    onVoiceMode: () -> Unit
) {
    val maxChars = 8000
    Column(
        Modifier.fillMaxWidth().padding(12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp, max = 140.dp).padding(vertical = 6.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("Ask anything…", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyLarge)
                }
                inner()
            }
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ComposerIcon(Icons.Rounded.Image, "Gallery", onGallery)
            ComposerIcon(Icons.Rounded.Camera, "Camera", onCamera)
            ComposerIcon(Icons.Rounded.AttachFile, "Files", onFiles)
            if (voiceEnabled) ComposerIcon(Icons.Rounded.GraphicEq, "Voice mode", onVoiceMode)
            Box(Modifier.weight(1f))
            if (value.isNotEmpty()) {
                Text("${value.length}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(end = 8.dp))
            }
            if (isGenerating) {
                SendFab(Icons.Rounded.Stop, onStop)
            } else if (value.isBlank() && voiceEnabled) {
                ComposerIcon(Icons.Rounded.Mic, "Speak", onMic)
            } else if (value.isNotBlank()) {
                SendFab(Icons.Rounded.Send, onSend)
            }
        }
    }
}

@Composable
private fun ComposerIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Icon(
        icon, desc,
        modifier = Modifier.size(26.dp).padding(2.dp).clickableNoRipple(onClick),
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

@Composable
private fun SendFab(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
            .clickableNoRipple(onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
}

