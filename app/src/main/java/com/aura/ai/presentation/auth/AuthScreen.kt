package com.aura.ai.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.components.GradientButton
import com.aura.ai.core.ui.theme.GradientEnd
import com.aura.ai.core.ui.theme.GradientStart
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleClient = remember { GoogleSignInClient(context) }

    LaunchedEffect(state.authenticated) { if (state.authenticated) onAuthenticated() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(88.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(GradientStart, GradientEnd))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(44.dp)) }

            Spacer(Modifier.height(20.dp))
            Text("Aura AI", style = MaterialTheme.typography.displayLarge)
            Text("Your intelligent companion", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(state.mode == AuthMode.SIGN_UP) {
                Column {
                    OutlinedTextField(
                        value = state.name, onValueChange = viewModel::onNameChange,
                        label = { Text("Name") }, singleLine = true,
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            OutlinedTextField(
                value = state.email, onValueChange = viewModel::onEmailChange,
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password, onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            state.info?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = if (state.mode == AuthMode.SIGN_IN) "Sign In" else "Create Account",
                loading = state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::submitEmail
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = viewModel::forgotPassword) { Text("Forgot password?") }
                TextButton(onClick = viewModel::toggleMode) {
                    Text(if (state.mode == AuthMode.SIGN_IN) "Create account" else "Have an account? Sign in")
                }
            }

            Spacer(Modifier.height(4.dp))
            OutlinedButtonRow(
                onGoogle = {
                    scope.launch {
                        val token = googleClient.getIdToken()
                        if (token != null) viewModel.signInWithGoogle(token)
                    }
                },
                onGuest = viewModel::signInAnonymously
            )
        }
    }
}

@Composable
private fun OutlinedButtonRow(onGoogle: () -> Unit, onGuest: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        androidx.compose.material3.OutlinedButton(
            onClick = onGoogle, modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Continue with Google") }
        TextButton(onClick = onGuest, modifier = Modifier.fillMaxWidth()) {
            Text("Continue as Guest")
        }
    }
}
