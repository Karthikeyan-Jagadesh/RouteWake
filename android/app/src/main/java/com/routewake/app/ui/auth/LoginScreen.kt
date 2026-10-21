package com.routewake.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.routewake.app.data.repository.AuthRepository
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LoginContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onLoginSubmit = { email, password ->
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please enter both email and password."
                return@LoginContent
            }
            isLoading = true
            errorMessage = null
            scope.launch {
                val result = authRepository.login(email.trim(), password)
                isLoading = false
                result.onSuccess {
                    onLoginSuccess()
                }.onFailure { ex ->
                    errorMessage = ex.message ?: "Authentication failed"
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister,
        onContinueAsGuest = onContinueAsGuest
    )
}

@Composable
fun LoginContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginSubmit: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign in to synchronize your trips and alert settings",
            style = MaterialTheme.typography.bodyMedium,
            color = RouteWakeTextSecondaryDark
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = RouteWakeErrorRed.copy(alpha = 0.15f),
                border = null
            ) {
                Text(
                    text = errorMessage!!,
                    color = RouteWakeErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RouteWakeAccentLime) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RouteWakeAccentLime) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RouteWakeAccentLime)
            }
        } else {
            RouteWakeButton(
                text = "Log In",
                onClick = {
                    onLoginSubmit(email, password)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = RouteWakeTextSecondaryDark
            )
            Text(
                text = "Register",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RouteWakeAccentLime,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Explore App Offline / Guest Mode",
                style = MaterialTheme.typography.labelLarge,
                color = RouteWakeTextMutedDark,
                modifier = Modifier.clickable { onContinueAsGuest() }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Login Screen (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoginScreenDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        LoginContent(
            isLoading = false,
            errorMessage = null,
            onLoginSubmit = { _, _ -> },
            onNavigateToRegister = {},
            onContinueAsGuest = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Login Screen - Error State (Light)",
    showBackground = true
)
@Composable
private fun LoginScreenErrorLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        LoginContent(
            isLoading = false,
            errorMessage = "Invalid credentials. Please verify your email and password.",
            onLoginSubmit = { _, _ -> },
            onNavigateToRegister = {},
            onContinueAsGuest = {}
        )
    }
}

