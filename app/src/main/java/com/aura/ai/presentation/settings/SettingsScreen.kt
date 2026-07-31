package com.aura.ai.presentation.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.AuraChip
import com.aura.ai.core.ui.components.GlassCard
import com.aura.ai.core.ui.components.clickableNoRipple
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.ResponseStyle
import com.aura.ai.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val p = state.prefs

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.displayLarge) }

        item { SectionCard("Appearance") {
            LabeledRow("Theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { m ->
                    AuraChip(m.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = p.themeMode == m) { viewModel.setTheme(m) }
                }
            }
            ToggleRow("Dynamic colors (Material You)", p.dynamicColor, viewModel::setDynamicColor)
            ToggleRow("High contrast", p.highContrast, viewModel::setHighContrast)
        } }

        item { SectionCard("AI Preferences") {
            LabeledRow("Default model")
            Column {
                AiModel.entries.forEach { m ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickableNoRipple { viewModel.setModel(m.id) },
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(m.displayName, Modifier.weight(1f))
                        if (p.defaultModel == m.id) Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            SliderRow("Temperature", p.temperature, 0f..1.5f) { viewModel.setTemperature(it) }
            SliderRow("Top P", p.topP, 0f..1f) { viewModel.setTopP(it) }
            SliderRow("Max tokens", p.maxTokens.toFloat(), 256f..8192f) { viewModel.setMaxTokens(it.toInt()) }
            ToggleRow("Streaming responses", p.streaming, viewModel::setStreaming)
            ToggleRow("Conversation memory", p.memoryEnabled, viewModel::setMemory)
            ToggleRow("Reasoning mode", p.reasoningEnabled, viewModel::setReasoning)
            ToggleRow("Internet access", p.internetEnabled, viewModel::setInternet)
            LabeledRow("Response style")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ResponseStyle.entries.forEach { s ->
                    AuraChip(s.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = p.responseStyle == s) { viewModel.setStyle(s) }
                }
            }
        } }

        item { SectionCard("Voice") {
            ToggleRow("Voice input", p.voiceEnabled, viewModel::setVoice)
            ToggleRow("Text-to-speech playback", p.ttsEnabled, viewModel::setTts)
        } }

        item { SectionCard("Notifications") {
            ToggleRow("Daily reminder", p.dailyReminderEnabled, viewModel::setDailyReminder)
        } }

        item { SectionCard("About") {
            LabeledRow("Aura AI v1.0.0")
            Text("Powered by NVIDIA NIM",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        } }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 6.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Row {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(if (value >= 100) value.toInt().toString() else String.format("%.2f", value),
                color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun LabeledRow(label: String) {
    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
}


