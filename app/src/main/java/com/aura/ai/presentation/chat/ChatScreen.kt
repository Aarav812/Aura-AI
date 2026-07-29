package com.aura.ai.presentation.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.EmptyState
import com.aura.ai.core.ui.components.OfflineBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onOpenVoice: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Text-to-Speech engine
    val tts = remember { TextToSpeechController(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    // Speech-to-Text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        SpeechRecognizerHelper.parseResult(result)?.let { viewModel.onInputChange(state.input + it) }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addImage(it.toString()) } }

    // Auto-scroll to latest
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium)
                        Text(state.model.displayName, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                actions = { ModelSelectorButton(state.model, onSelect = viewModel::selectModel) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AiComposer(
                value = state.input,
                isGenerating = state.isGenerating,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::send,
                onStop = viewModel::stopGenerating,
                onMic = { speechLauncher.launch(SpeechRecognizerHelper.intent()) },
                onGallery = { imagePicker.launch("image/*") },
                onCamera = { imagePicker.launch("image/*") },
                onFiles = { imagePicker.launch("*/*") },
                onVoiceMode = onOpenVoice
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(!state.isOnline) { OfflineBanner() }
            if (state.messages.isEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    EmptyState(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "How can I help you today?",
                        subtitle = "Ask a question, paste some text, or attach a file to get started.",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isLast = msg.id == state.messages.lastOrNull()?.id,
                            onCopy = { copyToClipboard(context, msg.text) },
                            onRegenerate = viewModel::regenerate,
                            onSpeak = { tts.speak(msg.text) },
                            onFeedback = { viewModel.setFeedback(msg.id, it) }
                        )
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("message", text))
}
