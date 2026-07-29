package com.aura.ai.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aura.ai.core.ui.components.GlassCard
import com.aura.ai.core.ui.components.GradientButton
import com.aura.ai.core.ui.theme.GradientEnd
import com.aura.ai.core.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    var name by remember(user?.name) { mutableStateOf(user?.name ?: "") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val photo = user?.photoUrl
            if (photo != null) {
                AsyncImage(photo, "Avatar", modifier = Modifier.size(100.dp).clip(CircleShape))
            } else {
                Box(
                    Modifier.size(100.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text((user?.name?.firstOrNull() ?: 'A').uppercase(),
                        color = Color.White, style = MaterialTheme.typography.displayLarge)
                }
            }

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Email", user?.email ?: "—")
                    InfoRow("Plan", user?.plan?.displayName ?: "Free")
                    InfoRow("Account", if (user?.isAnonymous == true) "Guest" else "Registered")
                }
            }

            GradientButton("Save Changes", modifier = Modifier.fillMaxWidth()) { viewModel.updateName(name) }
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.TextButton(onClick = { viewModel.signOut(onSignedOut) }) { Text("Sign Out") }
            androidx.compose.material3.TextButton(onClick = { viewModel.deleteAccount(onSignedOut) }) {
                Text("Delete Account", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
