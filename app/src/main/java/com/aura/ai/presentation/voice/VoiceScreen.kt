package com.aura.ai.presentation.voice

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aura.ai.core.ui.components.clickableNoRipple
import com.aura.ai.core.ui.theme.GradientEnd
import com.aura.ai.core.ui.theme.GradientStart

/**
 * Modern voice interface: animated waveform + glow, live transcription placeholder,
 * continuous conversation toggle. Wake-word-ready (mic session managed here).
 */
@Composable
fun VoiceScreen(onBack: () -> Unit) {
    var listening by remember { mutableStateOf(true) }
    var transcript by remember { mutableStateOf("Listening…") }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Close, "Close",
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(28.dp).clickableNoRipple(onBack))

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)) {

            VoiceOrb(active = listening)
            Waveform(active = listening)

            Text(transcript, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 40.dp))

            Text(if (listening) "Tap to pause" else "Tap to speak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.clickableNoRipple {
                    listening = !listening
                    transcript = if (listening) "Listening…" else "Paused"
                })
        }
    }
}

@Composable
private fun VoiceOrb(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "orb")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = if (active) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "orbScale"
    )
    Box(
        Modifier.size((120 * scale).dp).clip(CircleShape)
            .background(Brush.radialGradient(listOf(GradientStart, GradientEnd))),
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Rounded.Mic, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
}

@Composable
private fun Waveform(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = if (active) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "wavePhase"
    )
    val primary = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 40.dp)) {
        val bars = 28
        val gap = size.width / bars
        for (i in 0 until bars) {
            val h = (10 + (Math.sin(i * 0.7 + phase * 6) + 1) * 22 * phase).toFloat()
            drawRoundRect(
                color = primary,
                topLeft = androidx.compose.ui.geometry.Offset(i * gap, size.height / 2 - h / 2),
                size = androidx.compose.ui.geometry.Size(gap * 0.4f, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}
